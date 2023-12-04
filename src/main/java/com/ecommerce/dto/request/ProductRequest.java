package com.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductRequest (Long id, String title, Double price, String category, String description) {
}
