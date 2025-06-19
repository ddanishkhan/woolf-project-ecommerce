package com.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductRequest(@NotBlank String name, @NotNull Double price, @NotNull UUID categoryId,
                             @NotNull String description, @NotNull Integer quantity,
                             @JsonProperty("image") String imageURL) {
}
