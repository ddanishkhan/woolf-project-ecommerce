package com.ecommerce.ordermanagement.events.listener;

import com.ecommerce.ordermanagement.config.KafkaTopicConfig;
import com.ecommerce.ordermanagement.events.dto.PaymentProcessedEvent;
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
public class PaymentResultListener {

    private final OrderRepository orderRepository;

    @KafkaListener(
            containerFactory = "paymentResultListenerContainerFactory",
            topics = KafkaTopicConfig.PAYMENT_RESULTS_TOPIC,
            groupId = "order-management-group")
    public void handlePaymentResult(PaymentProcessedEvent event) {
        log.info("Received payment result for order ID: {}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            log.warn("Order not found or not awaiting payment for ID: {}", event.getOrderId());
            return;
        }

        if (event.isSuccess()) {
            order.setStatus(OrderStatus.PAID);
            log.info("Order {} status updated to PAID.", order.getId());
            // TODO trigger shipping, send a receipt email, etc.
        } else if (!event.isSuccess() && event.isCancelOrder()) {
            order.setStatus(OrderStatus.CANCELLED);
            // TODO : reconciliation process to refill the ordered items back
        } else {
            // If payment fails, we set the status but do NOT cancel yet.
            // The order remains in this state, allowing the user to retry.
            // TODO Add a scheduled cancellation job will eventually clean the failed orders.
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            log.error("Order {} status updated to PAYMENT_FAILED. Reason: {}", order.getId(), event.getFailureReason());
        }

        orderRepository.save(order);
    }
}
