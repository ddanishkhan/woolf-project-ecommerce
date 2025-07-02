package com.ecommerce.cartservice.external.api.ordermanagement;

import com.ecommerce.dtos.order.OrderResponse;
import com.ecommerce.dtos.order.CreateOrderRequest;

public interface IOrderManagementService {
    OrderResponse createOrder(CreateOrderRequest orderRequest, String token);
}
