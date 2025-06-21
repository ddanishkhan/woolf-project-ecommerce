package com.ecommerce.ordermanagement.events.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ReleaseStockEvent implements Serializable {
    private Long orderId;
    private List<OrderItem> items;
}

