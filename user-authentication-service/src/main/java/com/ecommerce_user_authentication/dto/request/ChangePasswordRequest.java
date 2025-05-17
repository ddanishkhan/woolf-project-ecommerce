package com.ecommerce_user_authentication.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record ChangePasswordRequest(@NotEmpty String email, @NotEmpty String password, @NotEmpty String newPassword) {
}
