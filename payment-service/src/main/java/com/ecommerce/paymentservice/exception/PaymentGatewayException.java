package com.ecommerce.paymentservice.exception;

public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message, Throwable e) {
        super(message, e);
    }
}
