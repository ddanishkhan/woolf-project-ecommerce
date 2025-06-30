package com.ecommerce.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class StockUpdateRequest {
    @Min(1) private int quantity;
}
