package com.ecommerce.controller;

import com.ecommerce.dto.response.ProductResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
public class ProductController {

    private final RestTemplateBuilder restTemplateBuilder;
    private static final String FAKESTOREAPI_COM = "https://fakestoreapi.com";

    public ProductController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @GetMapping("/products")
    public List<ProductResponse> getAllProducts() {
        String getAllProductsURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).path("products").build().toUriString();
        var restTemplate = restTemplateBuilder.build();
        var response = restTemplate.exchange(getAllProductsURL, HttpMethod.GET, null, new ParameterizedTypeReference<List<ProductResponse>>() {
        });
        return response.getBody();
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Integer id) {
        var getProductURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).pathSegment("products", String.valueOf(id));
        var response = restTemplateBuilder.build().getForEntity(getProductURL.toUriString(), ProductResponse.class);
        return response.getBody();
    }

}
