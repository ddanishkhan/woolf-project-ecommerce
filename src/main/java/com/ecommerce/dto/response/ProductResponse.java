package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponse(Long id, String title, Double price, String category, String description, @JsonProperty("image") String imageURL) {}
