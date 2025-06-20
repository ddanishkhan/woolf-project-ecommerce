package com.ecommerce.ordermanagement.events.listener;

import com.ecommerce.ordermanagement.config.KafkaTopicConfig;
import com.ecommerce.ordermanagement.events.dto.OrderConfirmedEvent;
import com.ecommerce.ordermanagement.events.dto.StockReservationEvent;
import com.ecommerce.ordermanagement.model.Order;
import com.ecommerce.ordermanagement.model.OrderStatus;
import com.ecommerce.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaResultListener {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    @KafkaListener(
            containerFactory = "stockReservationListenerContainerFactory",
            topics = KafkaTopicConfig.STOCK_RESULTS_TOPIC,
            groupId = "order-management-group"
    )
    public void handleStockReservationResult(StockReservationEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING) return;

        if (event.isSuccess()) {
            order.setStatus(OrderStatus.AWAITING_PAYMENT);
            orderRepository.save(order);

            // Stock is reserved, now trigger the payment process
            OrderConfirmedEvent paymentEvent = new OrderConfirmedEvent();
            paymentEvent.setOrderId(order.getId());
            paymentEvent.setTotalAmount(order.getTotalAmount());
            // This token comes from the frontend client (e.g., Stripe.js)
//            paymentEvent.setPaymentMethodToken(order.getPaymentMethodToken());
            paymentEvent.setPaymentMethodToken("tok_visa"); // Using a test token for now

            kafkaTemplate.send(KafkaTopicConfig.ORDER_CONFIRMED_TOPIC, paymentEvent);

        } else {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }
    }
}
