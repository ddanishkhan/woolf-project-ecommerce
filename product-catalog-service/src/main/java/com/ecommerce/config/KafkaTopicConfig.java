package com.ecommerce.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDERS_STOCK_RESERVATION_RESERVE = "orders.stock.reservation.reserve";
    public static final String ORDERS_STOCK_RESERVATION_RELEASE = "orders.stock.reservation.release";
    public static final String STOCK_RESULTS_TOPIC = "stock.reservation.results";

    @Bean
    public NewTopic stockResultsTopic() {
        return TopicBuilder.name(STOCK_RESULTS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
