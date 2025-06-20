package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.config.KafkaConfig;
import com.ecommerce.paymentservice.events.dto.OrderConfirmedEvent;
import com.ecommerce.paymentservice.events.dto.PaymentProcessedEvent;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.model.PaymentStatus;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public void processPayment(OrderConfirmedEvent event) {
        // Idempotency Check: Don't process the same order twice
        if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("Payment for order {} already processed.", event.getOrderId());
            return;
        }

        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setAmount(event.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        Payment savedPayment = paymentRepository.save(payment);

        PaymentProcessedEvent paymentResultEvent = new PaymentProcessedEvent();
        paymentResultEvent.setOrderId(event.getOrderId());
        paymentResultEvent.setPaymentId(savedPayment.getId());

        try {
            // Create a charge with Stripe
            ChargeCreateParams params = ChargeCreateParams.builder()
                    .setAmount(event.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue()) // Amount in cents
                    .setCurrency("usd")
                    .setDescription("Payment for order " + event.getOrderId())
                    .setSource(event.getPaymentMethodToken()) // e.g., "tok_visa"
                    .build();

            Charge charge = Charge.create(params);

            // Payment Successful
            savedPayment.setStatus(PaymentStatus.SUCCESSFUL);
            savedPayment.setTransactionId(charge.getId());
            paymentResultEvent.setSuccess(true);
            paymentResultEvent.setTransactionId(charge.getId());

            log.info("Payment successful for order {}. Stripe Transaction ID: {}", event.getOrderId(), charge.getId());

        } catch (StripeException e) {
            // Payment Failed
            savedPayment.setStatus(PaymentStatus.FAILED);
            paymentResultEvent.setSuccess(false);
            paymentResultEvent.setFailureReason(e.getMessage());
            log.error("Payment failed for order {}: {}", event.getOrderId(), e.getMessage());
        }

        paymentRepository.save(savedPayment);
        // Publish the result to Kafka
        kafkaTemplate.send(KafkaConfig.PAYMENT_PROCESSED_TOPIC, paymentResultEvent);
    }
}
