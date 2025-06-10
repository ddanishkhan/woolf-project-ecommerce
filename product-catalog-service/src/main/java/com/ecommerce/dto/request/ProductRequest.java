package com.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ProductRequest(String name, Double price, UUID categoryId, String description, @JsonProperty("image") String imageURL) {}
