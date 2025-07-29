package com.ecommerce.ordermanagement.controller;

import com.ecommerce.common.dtos.CustomPageDTO;
import com.ecommerce.common.dtos.order.CreateOrderRequest;
import com.ecommerce.common.dtos.order.OrderResponse;
import com.ecommerce.common.dtos.order.OrderStatus;
import com.ecommerce.ordermanagement.service.OrderService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders : Creates a new order.
     *
     * @param orderRequest The request body containing order details.
     * @return The created order with HTTP status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest orderRequest,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
            Authentication authentication) {

        // The principal is the user ID string we set in the JWT filter
        Long userId = Long.parseLong(authentication.getName());

        OrderResponse createdOrder = orderService.createOrder(userId, orderRequest, authHeader);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    // Fetches orders for the currently authenticated user
    @GetMapping
    public ResponseEntity<CustomPageDTO<OrderResponse>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = Long.parseLong(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(userId, pageable));
    }

    /**
     * GET /api/orders/{id} : Retrieves a single order by its ID.
     *
     * @param id The ID of the order.
     * @return The order details with HTTP status 200 (OK).
     */
    @GetMapping("/{id}")
    @PreAuthorize("@orderSecurityService.isOwner(authentication, #id) or hasAnyRole('ADMIN', 'SERVICE')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }


    /**
     * GET /api/orders/customer/{customerId} : Retrieves all orders for a customer.
     *
     * @param customerId The ID of the customer.
     * @return A list of orders with HTTP status 200 (OK).
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomPageDTO<OrderResponse>> getOrdersByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId, pageable));
    }

    /**
     * PUT /api/orders/{id}/status : Updates the status of an order.
     *
     * @param id            The ID of the order to update.
     * @param status The request body containing the new status.
     * @return The updated order details with HTTP status 200 (OK).
     */
    @PatchMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')") // Only Admins can update status
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
