package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.client.constants.MetadataConstant;
import com.ecommerce.paymentservice.client.dto.OrderDetailsDto;
import com.ecommerce.paymentservice.config.StripeApiProperties;
import com.ecommerce.paymentservice.dto.PaymentResponse;
import com.ecommerce.paymentservice.exception.PaymentGatewayException;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.model.PaymentStatus;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service("stripePaymentGatewayService")
@RequiredArgsConstructor
public class StripePaymentGatewayService implements PaymentGatewayService {

    private final PaymentRepository paymentRepository;
    private final StripeApiProperties stripeApiProperties;

    @Value("${app.domain}")
    private String appDomain;

    @Override
    public PaymentResponse createPayment(OrderDetailsDto orderDetails, Long orderId, Payment payment) {
        Stripe.apiKey = stripeApiProperties.getStripeSecretKey();

        String successUrl = UriComponentsBuilder.fromUriString(appDomain)
                .path("/api/payments/callback/success")
                .queryParam("orderId", orderId)
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .queryParam(MetadataConstant.PAYMENT_ID, payment.getId())
                .build()
                .toUriString();

        String cancelUrl = UriComponentsBuilder.fromUriString(appDomain)
                .path("/api/payments/callback/cancel")
                .queryParam("orderId", orderId)
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .queryParam(MetadataConstant.PAYMENT_ID, payment.getId())
                .build()
                .toUriString();


        SessionCreateParams.LineItem genericLineItem =
                SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency(orderDetails.getCurrency().toLowerCase()) // Use currency from order
                                        .setUnitAmount(orderDetails.getTotalAmount()
                                                .multiply(BigDecimal.valueOf(100))
                                                .setScale(0, RoundingMode.HALF_UP) // rounds to nearest whole number
                                                .longValue()) // Stripe uses cents
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName("Payment for Order " + orderDetails.getOrderId())
                                                        .build()
                                        ).build()
                        ).setQuantity(1L)
                        .build();

        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addLineItem(genericLineItem);

        sessionBuilder.putMetadata(MetadataConstant.PAYMENT_ID, payment.getId().toString());
        sessionBuilder.putMetadata(MetadataConstant.ORDER_ID, orderDetails.getOrderId().toString());

        try {
            Session session = Session.create(sessionBuilder.build());

            // 2. Update payment transaction status to CREATED and store gateway session ID
            updatePayment(payment, session);

            PaymentResponse response = new PaymentResponse();
            response.setOrderId(orderId);
            response.setPaymentGateway("STRIPE");
            response.setCheckoutUrl(session.getUrl());
            response.setSessionId(session.getId());
            response.setPaymentId(payment.getId());
            response.setStatus("CREATED");
            return response;
        } catch (StripeException e) {
            // 3. Update payment transaction status to FAILED on error
            updateFailedPayment(e, payment);
            throw new PaymentGatewayException("Error creating Stripe checkout session: " + e.getMessage(), e);
        }
    }

    private void updateFailedPayment(StripeException e, Payment paymentTx) {
        paymentTx.setStatus(PaymentStatus.FAILED);
        paymentTx.setFailureReason("Stripe API Error: " + e.getMessage());
        paymentRepository.save(paymentTx);
    }

    private void updatePayment(Payment paymentTx, Session session) {
        paymentTx.setGatewaySessionId(session.getId());
        paymentTx.setStatus(PaymentStatus.CREATED);
        paymentTx.setGatewayResponse(session.toJson()); // Store raw response for audit
        paymentRepository.save(paymentTx);
    }

}