package com.ecommerce.ordermanagement.events.publisher;

import com.ecommerce.ordermanagement.config.KafkaTopicConfig;
import com.ecommerce.ordermanagement.events.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishOrderCreatedEvent(OrderEvent orderEvent) {
        log.info("Publishing order created event for order ID: {} | topic {}", orderEvent.getOrderId(), KafkaTopicConfig.ORDERS_TOPIC);
        kafkaTemplate.send(KafkaTopicConfig.ORDERS_TOPIC, orderEvent.getOrderId().toString(), orderEvent);
    }
}
