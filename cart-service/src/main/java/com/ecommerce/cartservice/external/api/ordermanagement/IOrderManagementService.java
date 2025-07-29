package com.ecommerce.cartservice.external.api.ordermanagement;

import com.ecommerce.common.dtos.order.OrderResponse;
import com.ecommerce.common.dtos.order.CreateOrderRequest;

public interface IOrderManagementService {
    OrderResponse createOrder(CreateOrderRequest orderRequest, String token);
}
