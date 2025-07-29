package com.ecommerce.events.publisher;

import com.ecommerce.config.KafkaTopicConfig;
import com.ecommerce.events.dto.StockReservationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockEventPublisher {

    private final KafkaTemplate<String, StockReservationEvent> kafkaTemplate;

    public void publishStockReservationEvent(StockReservationEvent event) {
        log.info("Publishing stock reservation result for order ID: {}", event.getOrderId());
        kafkaTemplate.send(KafkaTopicConfig.STOCK_RESULTS_TOPIC, event.getOrderId().toString(), event);
    }
}
