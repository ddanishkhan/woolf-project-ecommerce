package com.ecommerce.cartservice.external.api.ordermanagement;

import com.ecommerce.dtos.order.CreateOrderRequest;
import com.ecommerce.dtos.order.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderManagementService implements IOrderManagementService {
    private static final String PATH_API = "api";
    private static final String PATH_ORDER = "orders";
    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${ordermanagement.service.url}")
    private String orderManagementBaseUrl;

    @Override
    public OrderResponse createOrder(CreateOrderRequest orderRequest, String token) {
        String requestUrl = UriComponentsBuilder.fromUriString(orderManagementBaseUrl)
                .pathSegment(PATH_API)
                .pathSegment(PATH_ORDER)
                .build().toUriString();
        log.info("Create order url: {}", requestUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> requestEntity = new HttpEntity<>(orderRequest, headers);

        try {
            ResponseEntity<OrderResponse> response = restTemplateBuilder.build().exchange(
                    requestUrl,
                    HttpMethod.POST,
                    requestEntity,
                    OrderResponse.class
            );
            if (response.getBody() != null) {
                return response.getBody();
            } else {
                throw new BadRequestException("Order not created.");
            }
        } catch (Exception e) {
            // Handle exceptions like network issues, HTTP client errors (4xx, 5xx)
            log.error("Error calling create order API: {}", e.getMessage());
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }


}
