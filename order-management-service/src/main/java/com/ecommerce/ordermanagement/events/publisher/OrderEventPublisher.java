package com.ecommerce.ordermanagement.events.publisher;

import com.ecommerce.ordermanagement.config.KafkaTopicConfig;
import com.ecommerce.ordermanagement.events.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishOrderCreatedReserveStockEvent(OrderEvent orderEvent) {
        log.info("Publishing order created event for order ID: {} | topic {}", orderEvent.getOrderId(), KafkaTopicConfig.ORDERS_STOCK_RESERVATION_RESERVE);
        kafkaTemplate.send(KafkaTopicConfig.ORDERS_STOCK_RESERVATION_RESERVE, orderEvent.getOrderId().toString(), orderEvent);
    }

    public void publishOrderStockReleaseEvent(OrderEvent orderEvent) {
        log.info("Publishing order stock release event for order ID: {} | topic {}", orderEvent.getOrderId(), KafkaTopicConfig.ORDERS_STOCK_RESERVATION_RELEASE);
        kafkaTemplate.send(KafkaTopicConfig.ORDERS_STOCK_RESERVATION_RELEASE, orderEvent.getOrderId().toString(), orderEvent);
    }

}
