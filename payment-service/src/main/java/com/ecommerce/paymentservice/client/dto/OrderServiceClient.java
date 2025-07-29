package com.ecommerce.paymentservice.client.dto;

import com.ecommerce.paymentservice.exception.ExternalClientException;
import com.ecommerce.paymentservice.exception.ResourceNotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class OrderServiceClient {

    private final RestTemplate restTemplate;
    private final String orderManagementServiceBaseUrl;

    @Autowired
    public OrderServiceClient(@Value("${order-management.service.url}") String orderManagementServiceBaseUrl) {
        this.orderManagementServiceBaseUrl = orderManagementServiceBaseUrl;
        this.restTemplate = new RestTemplate();
    }

    public OrderDetailsDto getOrderDetails(Long orderId, String authToken) {
        log.info("Fetching order details for orderId: {}", orderId);
        String url = orderManagementServiceBaseUrl + "/api/orders/{orderId}";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<OrderDetailsDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    OrderDetailsDto.class,
                    orderId
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.error("Order not found: {} - {}", orderId, e.getMessage());
                throw new ResourceNotFound("Order not found: " + orderId, e);
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.error("Unauthorized access to order {}: {}", orderId, e.getMessage());
                throw e;
            }
            log.error("Client error fetching order details for orderId {}: {}", orderId, e.getMessage());
            throw new ExternalClientException("Client error fetching order details: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.error("Server error fetching order details for orderId {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Order service internal error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to fetch order details for orderId {}: {}", orderId, e.getMessage());
            throw new ExternalClientException("Failed to fetch order details: " + e.getMessage(), e);
        }
    }

}