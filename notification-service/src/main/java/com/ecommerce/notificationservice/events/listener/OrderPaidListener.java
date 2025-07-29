package com.ecommerce.notificationservice.events.listener;

import com.ecommerce.notificationservice.config.KafkaProperties;
import com.ecommerce.notificationservice.dto.OrderDetailsDTO;
import com.ecommerce.notificationservice.dto.OrderPaidEvent;
import com.ecommerce.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidListener {

    private final EmailService emailService;
    private final RestTemplate restTemplate;

    @Value("${order-management.service.url}")
    private String orderServiceUrl;

    @KafkaListener(
            containerFactory = "kafkaListenerContainerFactory",
            topics = KafkaProperties.ORDERS_RECEIPT_TOPIC,
            groupId = KafkaProperties.NOTIFICATION_SERVICE_GROUP)
    public void handleOrderPaidEvent(@Payload OrderPaidEvent event) {
        log.info("Received paid order event for order ID: {}", event.getOrderId());
        try {
            // 1. Fetch full order details from the order-management-service
            String url = orderServiceUrl + "/api/orders/" + event.getOrderId();
            OrderDetailsDTO orderDetails = restTemplate.getForObject(url, OrderDetailsDTO.class);

            if (orderDetails != null) {
                // 2. Send the receipt email
                emailService.sendReceiptEmail(event.getUserEmail(), orderDetails);
                log.info("Receipt sent successfully for order ID: {}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to process receipt for order ID: {}", event.getOrderId(), e);
        }
    }
}
