package com.ecommerce.ordermanagement.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem implements Serializable {
    private UUID productId;
    private int quantity;
}
