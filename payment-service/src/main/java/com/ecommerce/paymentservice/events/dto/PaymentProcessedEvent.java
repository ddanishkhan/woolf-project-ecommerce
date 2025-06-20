package com.ecommerce.paymentservice.events.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class PaymentProcessedEvent implements Serializable {
    private Long orderId;
    private UUID paymentId;
    private boolean success;
    private String transactionId;
    private String failureReason;
}
