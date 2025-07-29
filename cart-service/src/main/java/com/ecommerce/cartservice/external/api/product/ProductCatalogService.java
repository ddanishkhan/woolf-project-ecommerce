package com.ecommerce.cartservice.external.api.product;

import com.ecommerce.common.dtos.product.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Slf4j
@Component
public class ProductCatalogService implements IProductCatalogService {

    private static final String PRODUCTS = "products";
    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @Autowired
    public ProductCatalogService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Override
    public ProductResponse getProductDetail(UUID productId) {
        String requestUrl = UriComponentsBuilder.fromUriString(productServiceUrl)
                .pathSegment(PRODUCTS)
                .pathSegment(productId.toString()).build().toUriString();
        log.info("Get product details via url: {}", requestUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            var response = restTemplateBuilder.build().exchange(
                    requestUrl,
                    HttpMethod.GET,
                    requestEntity,
                    ProductResponse.class
            );
            if (response.getBody() != null) {
                return response.getBody();
            } else {
                throw new BadRequestException("Product does not exist.");
            }
        } catch (Exception e) {
            log.error("Error calling product API: {}", e.getMessage());
            throw new RuntimeException("Failed to get product: " + e.getMessage(), e);
        }
    }


}


