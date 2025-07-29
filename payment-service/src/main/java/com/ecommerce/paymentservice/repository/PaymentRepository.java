package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.paymentservice.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByGatewayPaymentId(String paymentIntentId);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime createdAt);
}
