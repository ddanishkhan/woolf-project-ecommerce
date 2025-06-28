package com.ecommerce.notificationservice.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class KafkaProperties {
    public static final String ORDERS_RECEIPT_TOPIC = "orders.paid.receipt";
    public static final String PWD_RESET_TOKEN_TOPIC = "user.password.reset";
    public static final String NOTIFICATION_SERVICE_GROUP = "notification-service-group";
    private KafkaProperties() {
        // constant fields
    }
}