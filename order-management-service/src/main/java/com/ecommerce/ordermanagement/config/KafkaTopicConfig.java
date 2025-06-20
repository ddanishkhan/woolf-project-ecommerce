package com.ecommerce.ordermanagement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDERS_TOPIC = "orders.created";
    public static final String STOCK_RESULTS_TOPIC = "stock.reservation.results";
    public static final String ORDER_CONFIRMED_TOPIC = "orders.confirmed"; // Topic to publish to for payment
    public static final String PAYMENT_RESULTS_TOPIC = "payments.processed"; // Topic to listen on for payment results

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(ORDERS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderConfirmedTopic() {
        return TopicBuilder.name(ORDER_CONFIRMED_TOPIC).build();
    }

}

