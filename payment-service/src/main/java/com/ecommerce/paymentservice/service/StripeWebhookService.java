package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.client.constants.MetadataConstant;
import com.ecommerce.paymentservice.config.StripeApiProperties;
import com.ecommerce.paymentservice.events.PaymentEventProducer;
import com.ecommerce.paymentservice.events.dto.PaymentProcessedEvent;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.model.PaymentStatus;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookService {
    private final PaymentEventProducer paymentEventProducer;
    private final StripeApiProperties stripeApiProperties;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void processEvent(Event event) {
        log.info("Processing Stripe event of type: {}", event.getType());

        // Deserialize the nested object inside the event
        StripeObject stripeObject;
        if (event.getDataObjectDeserializer().getObject().isPresent()) {
            stripeObject = event.getDataObjectDeserializer().getObject().get();
        } else {
            log.error("Stripe Webhook data object deserialization failed for event type: {}. Event ID: {}. Data: {}",
                    event.getType(), event.getId(), event.getData());
            throw new RuntimeException("Failed to deserialize Stripe event object.");
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                handleCheckoutSessionCompleted(session);
                break;
            case "payment_intent.succeeded":
                // not handled, since checkout.session.completed will have same info.
                break;
            case "payment_intent.payment_failed":
                PaymentIntent paymentIntentFailed = (PaymentIntent) stripeObject;
                handlePaymentIntentFailed(paymentIntentFailed);
                break;
            case "checkout.session.expired":
                Session expiredSession = (Session) stripeObject;
                handleCheckoutSessionExpired(expiredSession);
                break;
            default:
                log.warn("Unhandled Stripe event type: {}", event.getType());
                break;
        }
    }

    private void handleCheckoutSessionCompleted(Session session) {
        String paymentIdStr = session.getMetadata() != null ? session.getMetadata().get(MetadataConstant.PAYMENT_ID) : null;
        String orderIdStr = session.getMetadata() != null ? session.getMetadata().get(MetadataConstant.ORDER_ID) : null;
        Long orderId = (orderIdStr != null) ? Long.valueOf(orderIdStr) : null;

        if (paymentIdStr == null) {
            log.error("Stripe webhook: 'checkout.session.completed' event received with missing '{}' in metadata for session ID: {}", MetadataConstant.PAYMENT_ID, session.getId());
            return;
        }
        UUID paymentId = UUID.fromString(paymentIdStr);
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            log.warn("Stripe webhook: Payment record with ID {} not found for 'checkout.session.completed' event. Order ID: {}. Session ID: {}",
                    paymentIdStr, orderId, session.getId());
            return;
        }
        Payment payment = optionalPayment.get();

        if (payment.getStatus() == PaymentStatus.SUCCESSFUL || payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.REFUNDED || payment.getStatus() == PaymentStatus.CANCELLED) {
            log.info("Payment transaction {} already in final status {}. Skipping 'checkout.session.completed' webhook processing.", paymentIdStr, payment.getStatus());
            return;
        }

        String newStatusReason = null;
        boolean isSuccess;

        if ("paid".equals(session.getPaymentStatus())) {
            payment.setStatus(PaymentStatus.SUCCESSFUL);
            payment.setGatewayPaymentId(session.getPaymentIntent());
            payment.setGatewayResponse(session.toJson());
            isSuccess = true;
            log.info("Payment transaction {} updated to SUCCESS via 'checkout.session.completed' webhook. Order ID: {}", paymentIdStr, orderId);
        } else if ("unpaid".equals(session.getPaymentStatus())) {
            payment.setStatus(PaymentStatus.FAILED);
            newStatusReason = "Payment was not completed: " + session.getPaymentStatus();
            payment.setFailureReason(newStatusReason);
            isSuccess = false;
            log.warn("Stripe Checkout session {} completed with status: {}. Marked as FAILED. Order ID: {}", session.getId(), session.getPaymentStatus(), orderId);
        } else {
            log.warn("Unhandled payment_status '{}' for checkout.session.completed event. Session ID: {}", session.getPaymentStatus(), session.getId());
            payment.setStatus(PaymentStatus.FAILED);
            newStatusReason = "Unhandled payment status: " + session.getPaymentStatus();
            payment.setFailureReason(newStatusReason);
            isSuccess = false;
        }

        paymentRepository.save(payment);

        var eventToPublish = new PaymentProcessedEvent(
                payment.getOrderId(),
                payment.getId(),
                isSuccess,
                false,
                payment.getGatewayPaymentId(),
                newStatusReason
        );
        paymentEventProducer.sendPaymentProcessedEvent(eventToPublish);
        log.info("PaymentProcessedEvent ({}) published for order {} (transaction {}).", isSuccess ? "SUCCESS" : "FAILURE", payment.getOrderId(), paymentIdStr);
    }

    private void handlePaymentIntentFailed(PaymentIntent paymentIntent) {
        String paymentIdStr = paymentIntent.getMetadata() != null ? paymentIntent.getMetadata().get(MetadataConstant.PAYMENT_ID) : null;
        String orderIdStr = paymentIntent.getMetadata() != null ? paymentIntent.getMetadata().get(MetadataConstant.ORDER_ID) : null;
        Long orderId = (orderIdStr != null) ? Long.valueOf(orderIdStr) : null;

        if (paymentIdStr == null) {
            log.error("Stripe webhook: 'payment_intent.payment_failed' event received with missing '{}' in metadata for PaymentIntent ID: {}", MetadataConstant.PAYMENT_ID, paymentIntent.getId());
            return;
        }

        Optional<Payment> optionalPayment = paymentRepository.findById(UUID.fromString(paymentIdStr));
        if (optionalPayment.isEmpty()) {
            log.warn("Stripe webhook: Payment record with ID {} not found for 'payment_intent.payment_failed' event. Order ID: {}. PaymentIntent ID: {}",
                    paymentIdStr, orderId, paymentIntent.getId());
            return;
        }
        Payment payment = optionalPayment.get();

        if (payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.SUCCESSFUL || payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Payment transaction {} already in final status {}. Skipping 'payment_intent.payment_failed' webhook processing.", paymentIdStr, payment.getStatus());
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        String failureReason = paymentIntent.getLastPaymentError() != null ?
                paymentIntent.getLastPaymentError().getMessage() : "Payment failed for unknown reason.";
        payment.setFailureReason(failureReason);
        payment.setGatewayResponse(paymentIntent.toJson());
        paymentRepository.save(payment);
        log.info("Payment transaction {} updated to FAILED via 'payment_intent.payment_failed' webhook. Order ID: {}. Reason: {}", paymentIdStr, orderId, failureReason);

        PaymentProcessedEvent eventToPublish = new PaymentProcessedEvent(
                payment.getOrderId(),
                payment.getId(),
                false,
                false,
                payment.getGatewayPaymentId(),
                failureReason
        );
        paymentEventProducer.sendPaymentProcessedEvent(eventToPublish);
        log.info("PaymentProcessedEvent (FAILURE) published for order {} (transaction {}).", payment.getOrderId(), paymentIdStr);
    }

    private void handleCheckoutSessionExpired(Session session) {
        String paymentIdStr = session.getMetadata() != null ? session.getMetadata().get(MetadataConstant.PAYMENT_ID) : null;
        String orderIdStr = session.getMetadata() != null ? session.getMetadata().get(MetadataConstant.ORDER_ID) : null;
        Long orderId = (orderIdStr != null) ? Long.valueOf(orderIdStr) : null;

        if (paymentIdStr == null) {
            log.error("Stripe webhook: 'checkout.session.expired' event received with missing '{}' in metadata for session ID: {}", MetadataConstant.PAYMENT_ID, session.getId());
            return;
        }

        Optional<Payment> optionalPayment = paymentRepository.findById(UUID.fromString(paymentIdStr));
        if (optionalPayment.isEmpty()) {
            log.warn("Stripe webhook: Payment record with ID {} not found for 'checkout.session.expired' event. Order ID: {}. Session ID: {}",
                    paymentIdStr, orderId, session.getId());
            return;
        }
        Payment payment = optionalPayment.get();

        // Idempotency check
        if (payment.getStatus() == PaymentStatus.EXPIRED || payment.getStatus() == PaymentStatus.SUCCESSFUL || payment.getStatus() == PaymentStatus.FAILED || payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Payment transaction {} already in final or EXPIRED state ({}). Skipping 'checkout.session.expired' webhook processing.", paymentIdStr, payment.getStatus());
            return;
        }

        payment.setStatus(PaymentStatus.EXPIRED);
        payment.setFailureReason("Checkout session expired on Stripe.");
        payment.setGatewayResponse(session.toJson());
        paymentRepository.save(payment);
        log.info("Payment transaction {} updated to EXPIRED via 'checkout.session.expired' webhook. Order ID: {}", paymentIdStr, orderId);

        PaymentProcessedEvent eventToPublish = new PaymentProcessedEvent(
                payment.getOrderId(),
                payment.getId(),
                false,
                true,
                payment.getGatewayPaymentId(),
                "Checkout session expired."
        );
        paymentEventProducer.sendPaymentProcessedEvent(eventToPublish);
        log.info("PaymentProcessedEvent (EXPIRED) published for order {} (transaction {}).", payment.getOrderId(), paymentIdStr);
    }

}
