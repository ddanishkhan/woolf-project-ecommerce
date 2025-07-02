package com.ecommerce.external.api;

import com.ecommerce.dtos.product.ProductRequest;
import com.ecommerce.dtos.product.ProductResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component("fakeStoreClient")
public class FakeStoreProductStoreClient implements ProductStoreClient {

    private final RestTemplateBuilder restTemplateBuilder;
    public FakeStoreProductStoreClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    private static final String FAKESTOREAPI_COM = "https://fakestoreapi.com";
    private static final String PRODUCTS = "products";

    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        String getAllProductsURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).path(PRODUCTS).build().toUriString();
        var restTemplate = restTemplateBuilder.build();
        return restTemplate.exchange(getAllProductsURL, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
        });
    }

    public ResponseEntity<ProductResponse> getProductById(Integer id) {
        var getProductURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).pathSegment(PRODUCTS, String.valueOf(id));
        return restTemplateBuilder.build().getForEntity(getProductURL.toUriString(), ProductResponse.class);
    }

    public ResponseEntity<ProductResponse> createNewProduct(ProductRequest productRequest) {
        var createNewProductURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).path(PRODUCTS);
        return restTemplateBuilder.build().postForEntity(createNewProductURL.toUriString(), productRequest, ProductResponse.class);
    }

    public void deleteProductById(Integer id) {
        var deleteProductURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).pathSegment(PRODUCTS, String.valueOf(id));
        restTemplateBuilder.build().delete(deleteProductURL.toUriString());
    }

    public ResponseEntity<ProductResponse> updateProductById(Integer id, ProductRequest productRequest) {
        var updateProductURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).pathSegment(PRODUCTS, String.valueOf(id));
        HttpEntity<ProductRequest> request = new HttpEntity<>(productRequest);
        return restTemplateBuilder.build().exchange(updateProductURL.toUriString(), HttpMethod.PUT, request, ProductResponse.class);
    }

}
