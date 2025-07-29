
package com.ecommerce.ordermanagement.service;

import com.ecommerce.common.dtos.order.OrderStatus;
import com.ecommerce.ordermanagement.events.dto.OrderEvent;
import com.ecommerce.ordermanagement.events.publisher.OrderEventPublisher;
import com.ecommerce.ordermanagement.model.Order;
import com.ecommerce.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReconciliationJob {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void reconcilePaymentFailedOrders() {
        log.info("Starting payment-failed order reconciliation job.");

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Order> oldPaymentFailedOrders = orderRepository.findByStatusAndCreatedAtBefore(
                OrderStatus.PAYMENT_FAILED, oneHourAgo);

        if (oldPaymentFailedOrders.isEmpty()) {
            log.info("No payment-failed orders older than one hour to reconcile.");
            return;
        }

        for (Order order : oldPaymentFailedOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            OrderEvent orderEvent = new OrderEvent(
                    order.getId(),
                    order.getOrderItems().stream()
                            .map(item -> new com.ecommerce.ordermanagement.events.dto.OrderItem(
                                    item.getProductId(), item.getQuantity()))
                            .toList()
            );
            orderEventPublisher.publishOrderStockReleaseEvent(orderEvent);

            log.info("Order {} reconciled and cancelled. Stock release event published.", order.getId());
        }

        log.info("Finished payment-failed order reconciliation job. Reconciled {} orders.", oldPaymentFailedOrders.size());
    }
}
