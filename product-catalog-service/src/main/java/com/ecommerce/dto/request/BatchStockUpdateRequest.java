package com.ecommerce.dto.request;

import java.util.List;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchStockUpdateRequest {
    @Valid
    private List<StockUpdateItem> items;
}
