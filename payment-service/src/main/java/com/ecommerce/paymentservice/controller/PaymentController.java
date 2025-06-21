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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
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
        } catch (RuntimeException e) {
            log.error("Failed to fetch order details for {}: {}", orderId, e.getMessage());
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

        // Deserialize the nested object inside the event
        StripeObject stripeObject = null;
        if (event.getDataObjectDeserializer().getObject().isPresent()) {
            stripeObject = event.getDataObjectDeserializer().getObject().get();
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            // Log this severely, as it indicates a compatibility issue with Stripe's API version.
            log.error("Stripe Webhook data object deserialization failed for event type: {}. Data: {}", event.getType(), event.getData());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to deserialize event object");
        }

        // Handle the event based on its type
        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                handleCheckoutSessionCompleted(session);
                break;
//            All below are present in checkout.session.completed. with metadata.
//            case "payment_intent.succeeded":
//                PaymentIntent paymentIntentSucceeded = (PaymentIntent) stripeObject;
//                handlePaymentIntentSucceeded(paymentIntentSucceeded);
//                break;
//            case "payment_intent.payment_failed":
//                PaymentIntent paymentIntentFailed = (PaymentIntent) stripeObject;
//                handlePaymentIntentFailed(paymentIntentFailed);
//                break;
//            case "charge.refunded":
//                Charge chargeRefunded = (Charge) stripeObject;
//                handleChargeRefunded(chargeRefunded);
//                break;
            // Add more cases for other event types as needed (e.g., invoice.paid, customer.subscription.created)
            default:
                log.warn("Unhandled Stripe event type: {}", event.getType());
                break;
        }

        // Always return 200 OK to Stripe to acknowledge receipt of the event
        return ResponseEntity.ok().build();
    }

    private void handleCheckoutSessionCompleted(Session session) {
        String paymentIdStr = session.getMetadata() != null ? session.getMetadata().get(MetadataConstant.PAYMENT_ID) : null;
        String orderIdStr = session.getMetadata() != null ? session.getMetadata().get(MetadataConstant.ORDER_ID) : null;
        Long orderId = (orderIdStr != null) ? Long.valueOf(orderIdStr) : null;

        if (paymentIdStr == null) {
            log.error("Stripe webhook: 'checkout.session.completed' event received with missing 'payment_id' in metadata for session ID: {}", session.getId());
            // This is a critical error. You might need to manually reconcile or log for investigation.
            return;
        }

        UUID paymentId = UUID.fromString(paymentIdStr);
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            log.warn("Stripe webhook: Payment transaction with internal ID {} not found for 'checkout.session.completed' event. Order ID: {}. Session ID: {}",
                    paymentId, orderId, session.getId());
            // This might indicate an issue or a webhook for a payment not initiated by your system.
            return;
        }
        Payment payment = optionalPayment.get();

        // Idempotency check: Don't process if already in a final state
        if (payment.getStatus() == PaymentStatus.SUCCESSFUL || payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.REFUNDED || payment.getStatus() == PaymentStatus.CANCELLED) {
            log.info("Payment transaction {} already in final status {}. Skipping 'checkout.session.completed' webhook processing.", paymentId, payment.getStatus());
            return;
        }

        String newStatusReason = null;
        boolean isSuccess = false;

        if ("paid".equals(session.getPaymentStatus())) {
            payment.setStatus(PaymentStatus.SUCCESSFUL);
            payment.setGatewayPaymentId(session.getPaymentIntent()); // Store Stripe Payment Intent ID
            payment.setGatewayResponse(session.toJson()); // Store full session JSON
            isSuccess = true;
            log.info("Payment transaction {} updated to SUCCESS via 'checkout.session.completed' webhook. Order ID: {}", paymentId, orderId);
        } else if ("unpaid".equals(session.getPaymentStatus())) {
            payment.setStatus(PaymentStatus.FAILED); // Or an appropriate status like EXPIRED
            newStatusReason = "Payment was not completed: " + session.getPaymentStatus();
            payment.setFailureReason(newStatusReason);
            isSuccess = false;
            log.warn("Stripe Checkout session {} completed with status: {}. Marked as FAILED. Order ID: {}", session.getId(), session.getPaymentStatus(), orderId);
        } else {
            // Handle other payment_status values if necessary.
            // For example, "no_payment_required" might occur for free trials in subscription mode
            log.warn("Unhandled payment_status '{}' for checkout.session.completed event. Session ID: {}", session.getPaymentStatus(), session.getId());
            payment.setStatus(PaymentStatus.FAILED); // Default to failed if unhandled definitive status
            newStatusReason = "Unhandled payment status: " + session.getPaymentStatus();
            payment.setFailureReason(newStatusReason);
            isSuccess = false;
        }

        paymentRepository.save(payment);

        // Publish Kafka event based on final status
        PaymentProcessedEvent eventToPublish = new PaymentProcessedEvent(
                payment.getOrderId(),
                payment.getId(),
                isSuccess,
                newStatusReason
        );
        paymentEventProducer.sendPaymentProcessedEvent(eventToPublish);
        log.info("PaymentProcessedEvent ({}) published for order {} (transaction {}).", isSuccess ? "SUCCESS" : "FAILURE", payment.getOrderId(), paymentId);
    }

    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        String paymentIdStr = paymentIntent.getMetadata() != null ? paymentIntent.getMetadata().get(MetadataConstant.PAYMENT_ID) : null;
        String orderIdStr = paymentIntent.getMetadata() != null ? paymentIntent.getMetadata().get(MetadataConstant.ORDER_ID) : null;
        Long orderId = (orderIdStr != null) ? Long.valueOf(orderIdStr) : null;
        if (paymentIdStr == null) {
            log.info("paymentIntent metadata {}", paymentIntent.getMetadata());
            log.error("Stripe webhook: 'payment_intent.succeeded' event received with missing '{}' in metadata for PaymentIntent ID: {}", MetadataConstant.PAYMENT_ID, paymentIntent.getId());
            return;
        }

        UUID paymentId = UUID.fromString(paymentIdStr);

        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            log.warn("Stripe webhook: Payment transaction with internal ID {} not found for 'payment_intent.succeeded' event. Order ID: {}. PaymentIntent ID: {}",
                    paymentId, orderId, paymentIntent.getId());
            return;
        }
        Payment payment = optionalPayment.get();

        if (payment.getStatus() == PaymentStatus.SUCCESSFUL || payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Payment transaction {} already in final status {}. Skipping 'payment_intent.succeeded' webhook processing.", paymentId, payment.getStatus());
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESSFUL);
        payment.setGatewayPaymentId(paymentIntent.getId()); // Store PaymentIntent ID as the definitive payment ID
        payment.setGatewayResponse(paymentIntent.toJson()); // Store full payment intent JSON
        paymentRepository.save(payment);
        log.info("Payment transaction {} updated to SUCCESS via 'payment_intent.succeeded' webhook. Order ID: {}", paymentId, orderId);

        // Publish Kafka event
        PaymentProcessedEvent eventToPublish = new PaymentProcessedEvent(
                payment.getOrderId(),
                payment.getId(),
                true,
                null
        );
        paymentEventProducer.sendPaymentProcessedEvent(eventToPublish);
        log.info("PaymentProcessedEvent (SUCCESS) published for order {} (transaction {}).", payment.getOrderId(), paymentId);
    }

    private void handlePaymentIntentFailed(PaymentIntent paymentIntent) {
        String paymentIdStr = paymentIntent.getMetadata() != null ? paymentIntent.getMetadata().get(MetadataConstant.PAYMENT_ID) : null;
        String orderIdStr = paymentIntent.getMetadata() != null ? paymentIntent.getMetadata().get(MetadataConstant.ORDER_ID) : null;
        Long orderId = (orderIdStr != null) ? Long.valueOf(orderIdStr) : null;
        UUID paymentId = (paymentIdStr != null) ? UUID.fromString(paymentIdStr) : null;

        if (paymentId == null) {
            log.error("Stripe webhook: 'payment_intent.payment_failed' event received with missing 'payment_id' in metadata for PaymentIntent ID: {}", paymentIntent.getId());
            return;
        }

        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            log.warn("Stripe webhook: Payment transaction with internal ID {} not found for 'payment_intent.payment_failed' event. Order ID: {}. PaymentIntent ID: {}",
                    paymentId, orderId, paymentIntent.getId());
            return;
        }
        Payment payment = optionalPayment.get();

        if (payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.SUCCESSFUL || payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Payment transaction {} already in final status {}. Skipping 'payment_intent.payment_failed' webhook processing.", paymentId, payment.getStatus());
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        // PaymentIntent.getLastPaymentError() can provide more details
        String failureReason = paymentIntent.getLastPaymentError() != null ?
                paymentIntent.getLastPaymentError().getMessage() : "Payment failed for unknown reason.";
        payment.setFailureReason(failureReason);
        payment.setGatewayResponse(paymentIntent.toJson());
        paymentRepository.save(payment);
        log.info("Payment transaction {} updated to FAILED via 'payment_intent.payment_failed' webhook. Order ID: {}. Reason: {}", paymentId, orderId, failureReason);

        // Publish Kafka event
        PaymentProcessedEvent eventToPublish = new PaymentProcessedEvent(
                payment.getOrderId(),
                payment.getId(),
                false,
                failureReason
        );
        paymentEventProducer.sendPaymentProcessedEvent(eventToPublish);
        log.info("PaymentProcessedEvent (FAILURE) published for order {} (transaction {}).", payment.getOrderId(), paymentId);
    }

//    private void handleChargeRefunded(Charge charge) {
//        // You'll need to link this back to a Payment.
//        // The charge object typically has a payment_intent ID which you can use to find your original Payment.
//        String paymentIntentId = charge.getPaymentIntent();
//        String refundReason = charge.getFailureMessage(); // Or charge.getRefunds().getData().get(0).getReason()
//
//        // Find the original payment transaction by gatewayPaymentId (which is the paymentIntentId in this case)
//        Optional<Payment> optionalPayment = paymentRepository.findByGatewayPaymentId(paymentIntentId);
//        if (optionalPayment.isEmpty()) {
//            log.warn("Stripe webhook: Original Payment not found for PaymentIntent ID {} during 'charge.refunded' event. Charge ID: {}", paymentIntentId, charge.getId());
//            return;
//        }
//        Payment payment = optionalPayment.get();
//
//        if (payment.getStatus() == PaymentStatus.REFUNDED) {
//            log.info("Payment transaction {} already REFUNDED. Skipping 'charge.refunded' webhook processing.", payment.getTransactionId());
//            return;
//        }
//
//        payment.setStatus(PaymentStatus.REFUNDED);
//        payment.setFailureReason("Refunded. Reason: " + (refundReason != null ? refundReason : "N/A"));
//        payment.setGatewayResponse(charge.toJson()); // Store full charge JSON
//        paymentRepository.save(payment);
//        log.info("Payment transaction {} (Order {}) updated to REFUNDED via 'charge.refunded' webhook. PaymentIntent ID: {}",
//                payment.getTransactionId(), payment.getOrderId(), paymentIntentId);
//
//        // Publish Kafka event for refund
//        PaymentProcessedEvent eventToPublish = new PaymentProcessedEvent(
//                payment.getOrderId(),
//                UUID.fromString(payment.getTransactionId()),
//                false, // A refund is typically considered a "non-success" for the original payment
//                payment.getTransactionId(),
//                "Refunded: " + (refundReason != null ? refundReason : "N/A")
//        );
//        paymentEventProducer.sendPaymentProcessedEvent(eventToPublish);
//        log.info("PaymentProcessedEvent (REFUNDED) published for order {} (transaction {}).", payment.getOrderId(), payment.getTransactionId());
//    }
}
