package com.ecommerce.ordermanagement.events.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents the outcome of a payment processing attempt.
 * This event is published by the Payment Service to a Kafka topic.
 */
@Data
public class PaymentProcessedEvent implements Serializable {
    private Long orderId;
    private UUID paymentId;
    private boolean success;
    private String transactionId; // The transaction ID from the payment gateway (e.g., Stripe)
    private String failureReason; // A message explaining why the payment failed, if applicable
}