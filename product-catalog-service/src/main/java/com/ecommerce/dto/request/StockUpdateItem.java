package com.ecommerce.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StockUpdateItem {

    @NotNull
    private UUID productId;

    @Min(1)
    private int quantity;

}

