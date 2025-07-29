package com.ecommerce.paymentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String paymentGateway;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = true) // Nullable if not applicable for all gateways
    private String gatewaySessionId; // Stripe Session ID, Razorpay Order ID

    @Column
    private String gatewayPaymentId; // Actual payment ID from gateway after successful transaction (e.g., Stripe PaymentIntent ID)

    @Column(columnDefinition = "TEXT")
    private String gatewayResponse; // Store raw response for debugging/audit

    @Column(columnDefinition = "TEXT")
    private String failureReason; // Detailed reason for failure

    @CreatedDate
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Payment(Long orderId, String paymentGateway, BigDecimal amount, String currency, PaymentStatus status) {
        this.orderId = orderId;
        this.paymentGateway = paymentGateway;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

}


