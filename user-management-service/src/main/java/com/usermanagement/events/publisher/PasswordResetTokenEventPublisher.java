package com.usermanagement.events.publisher;

import com.usermanagement.config.KafkaTopicConfig;
import com.usermanagement.events.dto.PasswordResetTokenEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenEventPublisher {

    private final KafkaTemplate<String, PasswordResetTokenEvent> kafkaTemplate;

    public void publishEvent(PasswordResetTokenEvent resetTokenEvent) {
        log.info("Publish event for user: {} | topic {}", resetTokenEvent.getUserEmail(), KafkaTopicConfig.PWD_RESET_TOKEN_TOPIC);
        kafkaTemplate.send(KafkaTopicConfig.PWD_RESET_TOKEN_TOPIC, resetTokenEvent.getUserEmail(), resetTokenEvent);
    }
}

