package com.ecommerce.notificationservice.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PasswordResetTokenEvent implements Serializable {
    private String userEmail;
    private String resetLink;
    private LocalDateTime expiresAt;
}
