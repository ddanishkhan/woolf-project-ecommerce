package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.client.dto.OrderDetailsDto;
import com.ecommerce.paymentservice.dto.PaymentResponse;
import com.ecommerce.paymentservice.model.Payment;

public interface PaymentGatewayService {

    String STRIPE = "STRIPE";

    PaymentResponse createPayment(OrderDetailsDto orderDetails, Long orderId, Payment payment);
    boolean cancelPayment(String sessionId);
}
