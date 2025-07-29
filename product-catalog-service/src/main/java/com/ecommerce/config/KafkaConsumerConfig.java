package com.ecommerce.config;

import com.ecommerce.events.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value(value = "${kafka.backoff.interval.ms}")
    private Long interval;

    @Value(value = "${kafka.backoff.max_failure}")
    private Long maxAttempts;

    public static final String GROUP_ID = "product-catalog-group";

    @Bean
    public ConsumerFactory<String, OrderEvent> orderEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.ecommerce.events.dto.OrderEvent");
        props.put(JsonDeserializer.TRUSTED_PACKAGES,
                "com.ecommerce.events.dto," +           // consumer's package
                        "com.ecommerce.ordermanagement.events.dto"); // producer's package

        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Equivalent to setRemoveTypeHeaders(false)
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> orderEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderEventConsumerFactory());
        // Retry configuration
        factory.setCommonErrorHandler(errorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        BackOff fixedBackOff = new FixedBackOff(interval, maxAttempts);
        return new DefaultErrorHandler(
                // logic to execute when all the retry attempts are exhausted
                (consumerRecord, exception) -> log.error("Message could not be processed."), fixedBackOff
        );
    }

}
