package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.dto.CreateOrderRequest;
import com.ecommerce.ordermanagement.dto.OrderItemRequest;
import com.ecommerce.ordermanagement.dto.OrderItemResponse;
import com.ecommerce.ordermanagement.dto.OrderResponse;
import com.ecommerce.ordermanagement.dto.ProductDto;
import com.ecommerce.ordermanagement.dto.UpdateOrderStatusRequest;
import com.ecommerce.ordermanagement.dto.UserDto;
import com.ecommerce.ordermanagement.events.dto.OrderEvent;
import com.ecommerce.ordermanagement.events.publisher.OrderEventPublisher;
import com.ecommerce.ordermanagement.exception.OrderProcessingException;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.exception.ServiceCommunicationException;
import com.ecommerce.ordermanagement.model.Customer;
import com.ecommerce.ordermanagement.model.Order;
import com.ecommerce.ordermanagement.model.OrderItem;
import com.ecommerce.ordermanagement.model.OrderStatus;
import com.ecommerce.ordermanagement.repository.CustomerRepository;
import com.ecommerce.ordermanagement.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderEventPublisher orderEventPublisher;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @SneakyThrows
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request, String authHeader) {
        // Find the customer locally or fetch and replicate them.
        // Possible to use kafka to publish new customer to this service to store local copy?
        Customer customer = findOrCreateCustomer(userId, authHeader);

        Order newOrder = new Order();
        newOrder.setCustomer(customer);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus(OrderStatus.PENDING);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            // 1. Call the Product Service to get product details
            ProductDto product = fetchProductDetails(itemRequest.productId(), authHeader);

            // 2. Check for sufficient stock
            if (product == null || product.stockQuantity() < itemRequest.quantity()) {
                throw new OrderProcessingException("Insufficient stock for product: " + product);
            }

            // 3. Create a new OrderItem with data from the Product Service
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.id());
            orderItem.setProductName(product.name());
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setPriceAtTimeOfOrder(product.price());
            newOrder.addOrderItem(orderItem);

            totalAmount = totalAmount.add(product.price().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }
        newOrder.setTotalAmount(totalAmount);

        // Save the order in its PENDING state. This gives us an orderId.
        Order savedOrder = orderRepository.save(newOrder);

        // Now, create and publish the OrderCreatedEvent for the Saga
        OrderEvent orderEvent = new OrderEvent(
                savedOrder.getId(),
                savedOrder.getOrderItems().stream()
                        .map(item -> new com.ecommerce.ordermanagement.events.dto.OrderItem(item.getProductId(), item.getQuantity()))
                        .collect(Collectors.toList())
        );
        log.info("Publish event {}", objectMapper.writeValueAsString(orderEvent));
        orderEventPublisher.publishOrderCreatedEvent(orderEvent);

        return convertToOrderResponse(savedOrder);
    }

    /**
     * Implements the "Replication on First Write" pattern.
     * Tries to find a customer in the local database. If not found, it fetches the
     * customer details from the User Management Service, saves a copy locally, and returns it.
     *
     * @param userId     The ID of the user from the JWT.
     * @param authHeader
     * @return The local Customer entity, either pre-existing or newly replicated.
     */
    private Customer findOrCreateCustomer(Long userId, String authHeader) {
        // First, check if the customer already exists.
        Optional<Customer> existingCustomer = customerRepository.findById(userId);
        if (existingCustomer.isPresent()) {
            return existingCustomer.get();
        }

        // If not, fetch from the User Management Service.
        UserDto userDto = fetchUserDetailsFromRemote(userId, authHeader);

        Customer newCustomer = new Customer();
        newCustomer.setId(userDto.id());
        newCustomer.setUsername(userDto.username());
        newCustomer.setEmail(userDto.email());

        try {
            return customerRepository.save(newCustomer);
        } catch (DataIntegrityViolationException ex) {
            // This exception is caught if another thread just saved the customer
            // between our initial check and this save attempt (the race condition).
            // In this case, we simply fetch the now-existing customer from the DB and return it.
            return customerRepository.findById(userId)
                    .orElseThrow(() -> new OrderProcessingException("Failed to retrieve customer after race condition. UserID: " + userId));
        }
    }

    private UserDto fetchUserDetailsFromRemote(Long userId, String authHeader) {
        String url = userServiceUrl + "/profile/me";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<UserDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDto.class);
            UserDto userDto = response.getBody();

            if (userDto == null) {
                throw new ResourceNotFoundException("Could not retrieve user details from User Management Service.");
            }
            if (!userId.equals(userDto.id())) {
                throw new OrderProcessingException("User ID mismatch between JWT token and profile service response.");
            }
            return userDto;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("User profile not found in User Management Service.");
        } catch (Exception ex) {
            throw new ServiceCommunicationException("Error fetching user details from User Service: " + ex.getMessage());
        }
    }

    private ProductDto fetchProductDetails(UUID productId, String authHeader) {
        String url = UriComponentsBuilder.fromUriString(productServiceUrl)
                .pathSegment("products", productId.toString())
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ProductDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, ProductDto.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        } catch (Exception ex) {
            throw new ServiceCommunicationException("Error fetching product details from Product Service. " + ex.getMessage());
        }
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        return convertToOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::convertToOrderResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest statusRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setStatus(statusRequest.status());
        Order updatedOrder = orderRepository.save(order);

        return convertToOrderResponse(updatedOrder);
    }

    private OrderResponse convertToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPriceAtTimeOfOrder()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getUsername(),
                order.getOrderDate(),
                order.getCurrency(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses);
    }
}

