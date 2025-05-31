package com.ecommerce_user_authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetPasswordRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 8 characters long")
    //TODO Add other password complexity validation if needed (e.g., @Pattern)
    private String newPassword;

}
