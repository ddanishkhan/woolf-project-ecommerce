package com.ecommerce.paymentservice.events;

import com.ecommerce.paymentservice.config.KafkaConfig;
import com.ecommerce.paymentservice.events.dto.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    public void sendPaymentProcessedEvent(PaymentProcessedEvent event) {
        String paymentProcessedTopic = KafkaConfig.PAYMENT_PROCESSED_TOPIC;
        log.info("Sending PaymentProcessedEvent to topic:{} data:{}", paymentProcessedTopic, event);
        kafkaTemplate.send(paymentProcessedTopic, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent event to Kafka. Topic: {}, Partition: {}, Offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send event to Kafka for orderId {}: {}", event.getOrderId(), ex.getMessage(), ex);
                    }
                });
    }

}