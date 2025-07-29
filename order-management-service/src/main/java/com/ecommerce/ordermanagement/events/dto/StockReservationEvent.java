package com.ecommerce.ordermanagement.events.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationEvent implements Serializable {
    private Long orderId;
    private boolean success;
    private String failureReason;
}
