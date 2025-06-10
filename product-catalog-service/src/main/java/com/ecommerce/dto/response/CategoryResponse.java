package com.ecommerce.dto.response;

import com.ecommerce.model.CategoryEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.util.UUID;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record CategoryResponse(
        UUID id,
        String name
) {
    public static CategoryResponse from(CategoryEntity entity){
        return new CategoryResponse(entity.getId(), entity.getName());
    }
}
