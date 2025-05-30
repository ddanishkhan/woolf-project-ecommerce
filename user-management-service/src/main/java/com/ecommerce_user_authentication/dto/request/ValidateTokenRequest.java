package com.ecommerce_user_authentication.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record ValidateTokenRequest(@NotEmpty String token, @NotEmpty String email) {
}
