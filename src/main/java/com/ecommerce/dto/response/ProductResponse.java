package com.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.UUID;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponse(UUID id, String title, Double price, String category, String description, @JsonProperty("image") String imageURL) {}
