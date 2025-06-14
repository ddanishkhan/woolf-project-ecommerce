package com.ecommerce.ordermanagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO representing the data structure for a Product
 * received from the external Product Service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductDto(
        UUID id,
        String name,
        String description,
        String category,
        BigDecimal price,
        Integer stockQuantity
) {
}
