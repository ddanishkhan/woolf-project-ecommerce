package com.ecommerce.ordermanagement.events.listener;

import com.ecommerce.ordermanagement.config.KafkaTopicConfig;
import com.ecommerce.ordermanagement.events.dto.StockReservationEvent;
import com.ecommerce.ordermanagement.model.Order;
import com.ecommerce.dtos.order.OrderStatus;
import com.ecommerce.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReservationResultListener {

    private final OrderRepository orderRepository;

    @KafkaListener(
            containerFactory = "stockReservationListenerContainerFactory",
            topics = KafkaTopicConfig.STOCK_RESULTS_TOPIC,
            groupId = "order-management-group"
    )
    public void handleStockReservationResult(StockReservationEvent event) {
        log.info("Received StockReservationEvent for order id: {}", event.getOrderId());
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING) return;

        if (event.isSuccess()) {
            order.setStatus(OrderStatus.AWAITING_PAYMENT);
            orderRepository.save(order);
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }
    }
}
