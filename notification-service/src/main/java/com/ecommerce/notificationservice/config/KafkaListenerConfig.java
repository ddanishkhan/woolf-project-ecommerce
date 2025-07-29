package com.ecommerce.notificationservice.config;

import com.ecommerce.notificationservice.dto.OrderPaidEvent;
import com.ecommerce.notificationservice.dto.PasswordResetTokenEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaListenerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, OrderPaidEvent> orderDetailsDTOConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        JsonDeserializer<OrderPaidEvent> deserializer = new JsonDeserializer<>(OrderPaidEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("com.ecommerce");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPaidEvent> orderPaidEventConcurrentKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderPaidEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderDetailsDTOConsumerFactory());
        return factory;
    }


    @Bean
    public ConsumerFactory<String, PasswordResetTokenEvent> passwordResetTokenEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        JsonDeserializer<PasswordResetTokenEvent> deserializer = new JsonDeserializer<>(PasswordResetTokenEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("com.ecommerce");
        deserializer.setUseTypeMapperForKey(true);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PasswordResetTokenEvent> passwordResetTokenListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PasswordResetTokenEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(passwordResetTokenEventConsumerFactory());
        return factory;
    }

}
