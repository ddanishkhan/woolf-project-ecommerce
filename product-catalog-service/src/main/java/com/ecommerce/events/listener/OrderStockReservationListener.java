package com.ecommerce.events.listener;

import com.ecommerce.config.KafkaConsumerConfig;
import com.ecommerce.config.KafkaTopicConfig;
import com.ecommerce.dto.request.BatchStockUpdateRequest;
import com.ecommerce.dto.request.StockUpdateItem;
import com.ecommerce.events.dto.OrderEvent;
import com.ecommerce.events.dto.StockReservationEvent;
import com.ecommerce.events.publisher.StockEventPublisher;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStockReservationListener {

    private final ProductService productService;
    private final StockEventPublisher stockEventPublisher;

    @KafkaListener(
            containerFactory = "orderEventListenerContainerFactory",
            topics = KafkaTopicConfig.ORDERS_STOCK_RESERVATION_RESERVE,
            groupId = KafkaConsumerConfig.GROUP_ID
    )
    public void handleOrderStockReservationEvent(@Payload OrderEvent event) {
        log.info("Received order created event for order ID: {}", event.getOrderId());
        StockReservationEvent reservationResult;

        try {
            BatchStockUpdateRequest stockRequest = new BatchStockUpdateRequest();
            stockRequest.setItems(
                    event.getItems().stream()
                            .map(item -> new StockUpdateItem(item.getProductId(), item.getQuantity()))
                            .toList()
            );

            productService.decrementStockBatch(stockRequest);

            reservationResult = new StockReservationEvent(event.getOrderId(), true, null);
            log.info("Stock successfully reserved for order ID: {}", event.getOrderId());

        } catch (Exception e) {
            reservationResult = new StockReservationEvent(event.getOrderId(), false, e.getMessage());
            log.error("Stock reservation failed for order ID: {}. Reason: {}", event.getOrderId(), e.getMessage());
        }

        stockEventPublisher.publishStockReservationEvent(reservationResult);
    }

    @KafkaListener(
            containerFactory = "orderEventListenerContainerFactory",
            topics = KafkaTopicConfig.ORDERS_STOCK_RESERVATION_RELEASE,
            groupId = KafkaConsumerConfig.GROUP_ID
    )
    public void handleOrderStockReleaseEvent(@Payload OrderEvent event) {
        log.info("Received inventory restocking event for order ID: {}", event.getOrderId());

        BatchStockUpdateRequest stockRequest = new BatchStockUpdateRequest();
        stockRequest.setItems(
                event.getItems().stream()
                        .map(item -> new StockUpdateItem(item.getProductId(), item.getQuantity()))
                        .toList()
        );

        productService.incrementStockBatch(stockRequest);
        log.info("Stock successfully updated for order ID: {}", event.getOrderId());

    }

}
