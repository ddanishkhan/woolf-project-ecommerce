package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service("fake store")
public class FakeStoreProductService implements ProductService {

    private final RestTemplateBuilder restTemplateBuilder;
    private static final String FAKESTOREAPI_COM = "https://fakestoreapi.com";
    private static final String PRODUCTS = "products";

    public FakeStoreProductService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        String getAllProductsURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).path(PRODUCTS).build().toUriString();
        var restTemplate = restTemplateBuilder.build();
        var response = restTemplate.exchange(getAllProductsURL, HttpMethod.GET, null, new ParameterizedTypeReference<List<ProductResponse>>() {
        });
        return response.getBody();
    }

    @Override
    public ProductResponse getProductById(Integer id) {
        var getProductURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).pathSegment(PRODUCTS, String.valueOf(id));
        var response = restTemplateBuilder.build().getForEntity(getProductURL.toUriString(), ProductResponse.class);
        return response.getBody();
    }

    @Override
    public ProductResponse createNewProduct(ProductRequest productRequest) {
        var createNewProductURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).path(PRODUCTS);
        return restTemplateBuilder.build().postForEntity(createNewProductURL.toUriString(), productRequest, ProductResponse.class)
                .getBody();

    }

    @Override
    public boolean deleteProductById(Integer id) {
        var deleteProductURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).pathSegment(PRODUCTS, String.valueOf(id));
        restTemplateBuilder.build().delete(deleteProductURL.toUriString());
        return true;
    }

    @Override
    public ProductResponse updateProductById(Integer id, ProductRequest productRequest) {
        var updateProductURL = UriComponentsBuilder.fromUriString(FAKESTOREAPI_COM).pathSegment(PRODUCTS, String.valueOf(id));
        HttpEntity<ProductRequest> request = new HttpEntity<>(productRequest);
        return restTemplateBuilder.build().exchange(updateProductURL.toUriString(), HttpMethod.PUT, request, ProductResponse.class).getBody();
    }

}
