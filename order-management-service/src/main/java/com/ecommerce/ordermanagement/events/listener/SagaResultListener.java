package com.ecommerce.ordermanagement.events.listener;

import com.ecommerce.ordermanagement.config.KafkaTopicConfig;
import com.ecommerce.ordermanagement.events.dto.StockReservationEvent;
import com.ecommerce.ordermanagement.model.Order;
import com.ecommerce.ordermanagement.model.OrderStatus;
import com.ecommerce.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaResultListener {

    private final OrderRepository orderRepository;

    @KafkaListener(
            containerFactory = "stockReservationListenerContainerFactory",
            topics = KafkaTopicConfig.STOCK_RESULTS_TOPIC,
            groupId = "order-management-group"
    )
    public void handleStockReservationResult(StockReservationEvent event) {
        log.info("Received stock reservation result for order ID: {}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order not found or already processed for ID: {}", event.getOrderId());
            return;
        }

        if (event.isSuccess()) {
            order.setStatus(OrderStatus.CONFIRMED);
            log.info("Order {} confirmed.", order.getId());
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            log.warn("Order {} cancelled due to: {}", order.getId(), event.getFailureReason());
        }

        orderRepository.save(order);
    }
}
