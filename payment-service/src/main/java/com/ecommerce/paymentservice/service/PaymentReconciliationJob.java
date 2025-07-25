package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.config.StripeApiProperties;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.model.PaymentStatus;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationJob {

    private final PaymentRepository paymentRepository;
    private final StripeApiProperties stripeApiProperties;
    private final StripeWebhookService stripeWebhookService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void reconcileExpiredPayments() {
        log.info("Starting expired payment reconciliation job.");

        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(stripeApiProperties.getDurationInMinutes());
        List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.CREATED, expirationTime);

        if (expiredPayments.isEmpty()) {
            log.info("No expired payments to reconcile.");
            return;
        }

        for (Payment payment : expiredPayments) {
            try {
                Session session = Session.retrieve(payment.getGatewaySessionId());
                if (session.getStatus().equals("expired")) {
                    stripeWebhookService.processEvent(null);
                }
            } catch (StripeException e) {
                log.error("Error retrieving session from Stripe: {}", e.getMessage());
            }
        }

        log.info("Finished expired payment reconciliation job. Reconciled {} payments.", expiredPayments.size());
    }
}
