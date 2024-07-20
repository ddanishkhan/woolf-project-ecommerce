package com.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductRequest(String name, Double price, String category, String description, @JsonProperty("image") String imageURL) {}
