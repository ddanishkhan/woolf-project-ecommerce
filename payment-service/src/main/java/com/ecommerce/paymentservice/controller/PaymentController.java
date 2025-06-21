package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.client.constants.MetadataConstant;
import com.ecommerce.paymentservice.client.dto.OrderDetailsDto;
import com.ecommerce.paymentservice.client.dto.OrderServiceClient;
import com.ecommerce.paymentservice.config.StripeApiProperties;
import com.ecommerce.paymentservice.dto.PaymentRequest;
import com.ecommerce.paymentservice.dto.PaymentResponse;
import com.ecommerce.paymentservice.events.PaymentEventProducer;
import com.ecommerce.paymentservice.events.dto.PaymentProcessedEvent;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.model.PaymentStatus;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.ecommerce.paymentservice.service.PaymentGatewayService;
import com.ecommerce.paymentservice.service.StripeWebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final Map<String, PaymentGatewayService> paymentGatewayServices;
    private final StripeWebhookService stripeWebhookService;
    private final OrderServiceClient orderServiceClient;
    private final PaymentEventProducer paymentEventProducer;
    private final StripeApiProperties stripeApiProperties;
    private final ObjectMapper objectMapper;

    @PostMapping("/create-payment-session")
    public ResponseEntity<PaymentResponse> createPaymentSession(
            @Valid @RequestBody PaymentRequest request,
            @RequestParam(defaultValue = "STRIPE") String paymentGateway,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        Long orderId = request.getOrderId();
        String authToken = authorizationHeader.substring("Bearer ".length());

        // 1. Fetch order details from Order-Management-Service
        OrderDetailsDto orderDetails;
        try {
            orderDetails = orderServiceClient.getOrderDetails(orderId, authToken);
            log.info("Fetched order details {}", orderDetails);
        } catch (HttpClientErrorException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Failed to fetch order details for {}: {} | {}", orderId, e.getMessage(), e.getClass());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or cannot be processed: " + orderId);
        }

        // 2. Validate order status
        if (!"AWAITING_PAYMENT".equalsIgnoreCase(orderDetails.getStatus())) {
            log.warn("Order {} is not in AWAITING_PAYMENT status. Current status: {}", orderId, orderDetails.getStatus());
            var response = new PaymentResponse();
            response.setOrderId(orderId);
            response.setStatus(orderDetails.getStatus());
            response.setAdditionalInfo(Map.of("error", "Order might be already processed."));
            return ResponseEntity.badRequest().body(response);
        }

        // 3. [Idempotent check] If payment for order already exists return error, else save and proceed.
        var optPayment = paymentRepository.findByOrderId(orderId);
        if (optPayment.isPresent()) {
            var existingPayment = optPayment.get();
            var response = new PaymentResponse();
            response.setPaymentId(existingPayment.getId());
            response.setOrderId(existingPayment.getOrderId());
            response.setStatus(existingPayment.getStatus().toString());
            response.setPaymentGateway(existingPayment.getPaymentGateway());
            response.setPaymentGatewayId(existingPayment.getGatewayPaymentId());
            response.setSessionId(existingPayment.getGatewaySessionId());
            response.setAdditionalInfo(Map.of("error", "Payment request already exists for this order."));
            return ResponseEntity.badRequest().body(response);
        }

        Payment payment = new Payment(
                orderId,
                paymentGateway, // STRIPE / RAZORPAY
                orderDetails.getTotalAmount(),
                orderDetails.getCurrency(),
                PaymentStatus.PENDING
        );
        log.info("saving payment in repo {}", payment);
        payment = paymentRepository.save(payment); // Save to get the ID

        // 4. Select and call the appropriate payment gateway service
        PaymentGatewayService service = null;
        if ("STRIPE".equalsIgnoreCase(paymentGateway)) {
            service = paymentGatewayServices.get("stripePaymentGatewayService");
        } else if ("RAZORPAY".equalsIgnoreCase(paymentGateway)) {
            service = paymentGatewayServices.get("razorpayPaymentGatewayService");
        }

        if (service == null) {
            throw new IllegalArgumentException("Unsupported payment gateway: " + paymentGateway);
        }

        PaymentResponse response = service.createPayment(orderDetails, orderId, payment);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback/success")
    public ResponseEntity<String> paymentSuccessCallback(
            @RequestParam(value = MetadataConstant.PAYMENT_ID) String paymentIdStr,
            @RequestParam(value = MetadataConstant.ORDER_ID, required = false) Long orderId) {
        log.info("Payment success callback received for internal paymentId: {}", paymentIdStr);
        UUID paymentId = UUID.fromString(paymentIdStr);
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            log.error("Payment not found for paymentId: {}", paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payment transaction not found.");
        }
        Payment payment = optionalPayment.get();

        // Idempotency check for callbacks
        if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            log.info("Payment transaction {} already in SUCCESS status via webhook. Skipping callback update.", paymentId);
            return ResponseEntity.ok("Payment already processed for order " + payment.getOrderId() + ". Thank you!");
        }
        return ResponseEntity.ok("Payment processed.");
    }

    @GetMapping("/callback/cancel")
    public ResponseEntity<String> paymentCancelCallback(
            @RequestParam(value = MetadataConstant.PAYMENT_ID) UUID paymentId,
            @RequestParam(value = MetadataConstant.ORDER_ID, required = false) Long orderId) { // orderId now just for logging/convenience
        log.info("Payment cancel callback received for internal paymentId: {}", paymentId);

        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            log.error("Payment transaction not found for paymentId: {}", paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payment transaction not found.");
        }
        Payment payment = optionalPayment.get();

        // Idempotency check for callbacks
        if (payment.getStatus() == PaymentStatus.CANCELLED || payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            log.info("Payment transaction {} already in status {}. Skipping callback update.", paymentId, payment.getStatus());
            return ResponseEntity.ok("Payment already processed for order " + payment.getOrderId() + ".");
        }

        try {
            // TODO: Replace payment cancellation with checkout.session.expired webhook entirely or keep this as well ?
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setFailureReason("User cancelled payment on gateway.");
            paymentRepository.save(payment);
            log.info("Payment transaction {} updated to CANCELLED via callback.", paymentId);

            // Publish Kafka event
            PaymentProcessedEvent event = new PaymentProcessedEvent(
                    payment.getOrderId(),
                    paymentId,
                    false,
                    payment.getGatewayPaymentId(),
                    "User cancelled payment."
            );
            paymentEventProducer.sendPaymentProcessedEvent(event);
            log.info("PaymentProcessedEvent (CANCELLED) published for order {}.", payment.getOrderId());

            return ResponseEntity.ok("Payment cancelled for order " + payment.getOrderId() + ".");
        } catch (Exception e) {
            log.error("Failed to process cancel callback for transaction {} (order {}): {}", paymentId, payment.getOrderId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing payment cancellation for order " + payment.getOrderId());
        }
    }


    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        log.info("Stripe Webhook received. Payload length: {}", payload.length());
        Event event;

        try {
            // Verify and construct the event
            log.debug("Payload: {} Stripe-Signature: {}", payload, sigHeader);
            event = Webhook.constructEvent(payload, sigHeader, stripeApiProperties.getWebhookSigningKey());
            log.info("Stripe Webhook event constructed successfully. Type: {}", event.getType());
        } catch (SignatureVerificationException e) {
            log.error("Stripe Webhook signature verification failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) { // Catch any other unexpected errors during construction
            log.error("Unexpected error constructing Stripe webhook event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }

        stripeWebhookService.processEvent(event);

        // Always return 200 OK to Stripe to acknowledge receipt of the event
        return ResponseEntity.ok().build();
    }

}
