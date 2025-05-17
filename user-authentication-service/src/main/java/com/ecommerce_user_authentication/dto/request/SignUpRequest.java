package com.ecommerce_user_authentication.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record SignUpRequest(@NotEmpty(message = "Email cannot be empty.") String email,
                            @NotEmpty(message = "Password cannot be empty.") String password) {
}
