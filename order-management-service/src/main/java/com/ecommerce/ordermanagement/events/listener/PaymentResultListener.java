package com.ecommerce.ordermanagement.events.listener;

import com.ecommerce.ordermanagement.config.KafkaTopicConfig;
import com.ecommerce.ordermanagement.events.dto.OrderEvent;
import com.ecommerce.ordermanagement.events.dto.OrderReceiptGenerationEvent;
import com.ecommerce.ordermanagement.events.dto.PaymentProcessedEvent;
import com.ecommerce.ordermanagement.events.publisher.OrderEventPublisher;
import com.ecommerce.ordermanagement.events.publisher.OrderReceiptGenerationEventPublisher;
import com.ecommerce.ordermanagement.model.Order;
import com.ecommerce.dtos.order.OrderStatus;
import com.ecommerce.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentResultListener {

    private final KafkaTemplate<String, OrderReceiptGenerationEvent> kafkaTemplate;
    private final OrderReceiptGenerationEventPublisher paidEventPublisher;
    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    @KafkaListener(
            containerFactory = "paymentResultListenerContainerFactory",
            topics = KafkaTopicConfig.PAYMENT_RESULTS_TOPIC,
            groupId = "order-management-group")
    @Transactional
    public void handlePaymentResult(PaymentProcessedEvent event) {
        log.info("Received payment result | {}", event);

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            log.warn("Order not found or not awaiting payment for ID: {}", event.getOrderId());
            return;
        }

        if (event.isSuccess()) {
            order.setStatus(OrderStatus.PAID);
            log.info("Order {} status updated to PAID.", order.getId());
            paidEventPublisher.triggerReceiptGeneration(order);
        } else if (event.isCancelOrder()) {
            order.setStatus(OrderStatus.CANCELLED);
            OrderEvent orderEvent = new OrderEvent(
                    order.getId(),
                    order.getOrderItems().stream()
                            .map(item -> new com.ecommerce.ordermanagement.events.dto.OrderItem(item.getProductId(), item.getQuantity()))
                            .toList()
            );
            orderEventPublisher.publishOrderStockReleaseEvent(orderEvent);
        } else {
            // If payment fails, we set the status but do NOT cancel yet.
            // The order remains in this state, allowing the user to retry.
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            log.error("Order {} status updated to PAYMENT_FAILED. Reason: {}", order.getId(), event.getFailureReason());
        }

        orderRepository.save(order);
    }
}
