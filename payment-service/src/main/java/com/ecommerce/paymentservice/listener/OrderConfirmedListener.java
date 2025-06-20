package com.ecommerce.paymentservice.listener;

import com.ecommerce.paymentservice.config.KafkaConfig;
import com.ecommerce.paymentservice.events.dto.OrderConfirmedEvent;
import com.ecommerce.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConfirmedListener {

    private final PaymentService paymentService;

    @KafkaListener(
            containerFactory = "kafkaListenerContainerFactory",
            topics = KafkaConfig.ORDER_CONFIRMED_TOPIC,
            groupId = KafkaConfig.PAYMENT_PROCESSED_TOPIC_GROUP_ID)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        paymentService.processPayment(event);
    }
}