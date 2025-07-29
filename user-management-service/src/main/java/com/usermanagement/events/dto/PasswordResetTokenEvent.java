package com.usermanagement.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetTokenEvent implements Serializable {
    private String userEmail;
    private String resetLink;
    private LocalDateTime expiresAt;
}
