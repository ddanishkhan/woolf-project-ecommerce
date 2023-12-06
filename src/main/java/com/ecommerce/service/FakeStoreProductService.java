package com.ecommerce.service;

import com.ecommerce.dto.response.ProductResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service("fake store")
public class FakeStoreProductService implements ProductService {

    private final RestTemplateBuilder restTemplateBuilder;
    private static final String FAKESTOREAPI_COM = "https://fakestoreapi.com";

    public FakeStoreProductService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    public List<ProductResponse> getAllProducts() {
        String getAllProductsURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).path("products").build().toUriString();
        var restTemplate = restTemplateBuilder.build();
        var response = restTemplate.exchange(getAllProductsURL, HttpMethod.GET, null, new ParameterizedTypeReference<List<ProductResponse>>() {
        });
        return response.getBody();
    }

    public ProductResponse getProductById(Integer id) {
        var getProductURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).pathSegment("products", String.valueOf(id));
        var response = restTemplateBuilder.build().getForEntity(getProductURL.toUriString(), ProductResponse.class);
        return response.getBody();
    }

}
