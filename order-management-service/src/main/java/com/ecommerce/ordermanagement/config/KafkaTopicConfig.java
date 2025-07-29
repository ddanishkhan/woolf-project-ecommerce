package com.ecommerce.ordermanagement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDERS_STOCK_RESERVATION_RESERVE = "orders.stock.reservation.reserve";
    public static final String ORDERS_STOCK_RESERVATION_RELEASE = "orders.stock.reservation.release";
    public static final String STOCK_RESULTS_TOPIC = "stock.reservation.results";
    public static final String PAYMENT_RESULTS_TOPIC = "payments.processed"; // Topic to listen on for payment results
    public static final String RELEASE_STOCK_TOPIC = "stock.release";
    public static final String ORDER_CANCELLATION_TOPIC = "orders.cancellation.check";
    public static final String ORDERS_RECEIPT_TOPIC = "orders.paid.receipt";   // topic to send receipt

    @Bean
    public NewTopic ordersStockReservationReserveTopic() {
        return TopicBuilder.name(ORDERS_STOCK_RESERVATION_RESERVE)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ordersReceiptTopic() {
        return TopicBuilder.name(ORDERS_RECEIPT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

}

