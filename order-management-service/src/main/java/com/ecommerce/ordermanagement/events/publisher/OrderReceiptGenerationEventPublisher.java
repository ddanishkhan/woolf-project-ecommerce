package com.ecommerce.ordermanagement.events.publisher;

import com.ecommerce.ordermanagement.config.KafkaTopicConfig;
import com.ecommerce.ordermanagement.events.dto.OrderReceiptGenerationEvent;
import com.ecommerce.ordermanagement.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderReceiptGenerationEventPublisher {

    private final KafkaTemplate<String, OrderReceiptGenerationEvent> kafkaTemplate;

    // Create and publish the event to trigger receipt generation
    public void triggerReceiptGeneration(Order order) {
        String topic = KafkaTopicConfig.ORDERS_RECEIPT_TOPIC;
        try {
            var receiptEvent = new OrderReceiptGenerationEvent();
            receiptEvent.setOrderId(order.getId());
            receiptEvent.setUserEmail(order.getCustomer().getEmail());
            log.info("Send to topic: {} data:{}", topic, receiptEvent);
            kafkaTemplate.send(topic, String.valueOf(receiptEvent.getOrderId()), receiptEvent);
        }
        catch (Exception e){
            log.info("Error sending message to topic {}, error:{}", topic, e.getMessage(), e);
        }
    }
}
