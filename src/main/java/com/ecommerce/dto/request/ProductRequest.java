package com.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductRequest(Long id, String title, Double price, String category, String description, @JsonProperty("image") String imageURL) {}
