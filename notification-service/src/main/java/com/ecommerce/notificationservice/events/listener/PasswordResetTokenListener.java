package com.ecommerce.notificationservice.events.listener;

import com.ecommerce.notificationservice.config.KafkaProperties;
import com.ecommerce.notificationservice.dto.PasswordResetTokenEvent;
import com.ecommerce.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetTokenListener {

    private final EmailService emailService;

    @KafkaListener(
            containerFactory = "passwordResetTokenListenerFactory",
            topics = KafkaProperties.PWD_RESET_TOKEN_TOPIC,
            groupId = KafkaProperties.NOTIFICATION_SERVICE_GROUP)
    public void handlePasswordResetEvent(@Payload PasswordResetTokenEvent event) {
        log.info("Received event for email: {}", event.getUserEmail());
        try {
            emailService.sendPasswordResetEmail(event.getUserEmail(), event.getResetLink(), event.getExpiresAt());
            log.info("Reset email sent successfully for email: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to process reset password for email: {}", event.getUserEmail(), e);
        }
    }
}

