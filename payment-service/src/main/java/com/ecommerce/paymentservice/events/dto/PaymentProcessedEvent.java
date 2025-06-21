package com.ecommerce.paymentservice.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PaymentProcessedEvent implements Serializable {
    private Long orderId;
    private UUID paymentId;
    private boolean success;
    private String failureReason;
}
