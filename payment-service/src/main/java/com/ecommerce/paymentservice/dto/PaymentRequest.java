package com.ecommerce.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Order ID is mandatory")
    private Long orderId;
}
