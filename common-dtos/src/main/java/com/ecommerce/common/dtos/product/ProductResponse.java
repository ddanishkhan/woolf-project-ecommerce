package com.ecommerce.common.dtos.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price,
        String category,
        String description,
        Integer stockQuantity,
        @JsonProperty("image") String imageURL) {
}
