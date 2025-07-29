package com.ecommerce.ordermanagement.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {
    private Long orderId;
    private List<OrderItem> items;
}

