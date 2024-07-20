package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductListResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "productService")
    private ProductService productService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void getAllProductsReturnEmptyListWhenNoProductsAvailable() throws Exception {

        when(productService.getAllProducts()).thenReturn(new ProductListResponse(Collections.emptyList()));

        mockMvc.perform(get("/products"))
                .andExpect(status().is(200))
                .andExpect(content().string("""
                        {"products":[]}"""));
    }

    @Test
    void getAllProductsReturnProducts() throws Exception {

        ProductResponse product1 = new ProductResponse(UUID.fromString("feecadf2-e74c-4a06-9e32-2e6d757158b2"), "Laptop", 1000.0, "Electronics", "Best Laptop", "url.com");
        List<ProductResponse> productListResponseDTO = List.of(product1);
        when(productService.getAllProducts()).thenReturn(new ProductListResponse(productListResponseDTO));

        mockMvc.perform(get("/products"))
                .andExpect(status().is(200))
                .andExpect(content().string("""
                        {"products":[{"id":"feecadf2-e74c-4a06-9e32-2e6d757158b2","name":"Laptop","price":1000.0,"category":"Electronics","description":"Best Laptop","image":"url.com"}]}"""));
    }

    @Test
    void getProductById_Failure() throws Exception {
        UUID id = UUID.fromString("feecadf2-e74c-4a06-9e32-2e6d757158b2");
        when(productService.getProductById(id)).thenThrow(new ProductNotFoundException("Product Not Found"));
        mockMvc.perform(get("/products/" + id))
                .andExpect(status().is(404))
                .andExpect(content().string("""
                        {"message":"Product Not Found","messageCode":404}"""));
    }

    @Test
    void findProductByIdSuccess() throws Exception {
        var id = UUID.fromString("feecadf2-e74c-4a06-9e32-2e6d757158b2");
        var productResponse = new ProductResponse(id, "Laptop", 1000.0, "Electronics", "Best Laptop", "url.com");
        var respString = convertToJson(productResponse);

        when(productService.getProductById(id)).thenReturn(productResponse);
        mockMvc.perform(get("/products/" + id))
                .andExpect(status().is(200))
                .andExpect(content().string(respString));
    }

    @Test
    void getProductByName() throws Exception {
        var id = UUID.fromString("abccadf2-e74c-4a06-9e32-2e6d757158b2");
        var productName = "Laptop";
        var productResponse = new ProductResponse(id, productName, 1001.0, "Electronics", "Best Laptop", "url.com");
        var respString = convertToJson(productResponse);

        when(productService.getProductByName(productName)).thenReturn(productResponse);
        mockMvc.perform(get("/products/name/" + productName))
                .andExpect(status().is(200))
                .andExpect(content().string(respString));
    }

    @Test
    void addNewProduct() throws Exception {
        var id = UUID.fromString("abcdadf2-e74c-4a06-1111-2e6d757158b2");
        var productName = "Laptop";
        var productResponse = new ProductResponse(id, productName, 1001.0, "Electronics", "Best Laptop", "url.com");
        var respString = convertToJson(productResponse);
        ProductRequest productRequest = new ProductRequest(productName, 1001.0, "Electronics", "Best Laptop", "url.com");
        var requestJson = convertToJson(productResponse);
        when(productService.createNewProduct(productRequest)).thenReturn(productResponse);
        mockMvc.perform(post("/products").content(requestJson).contentType(APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(content().string(respString));
    }

    @Test
    void updateProductDetail() throws Exception {
        var id = UUID.fromString("abcdadf2-e74c-4a06-2222-2e6d757158b2");
        var productName = "Laptop";
        var productResponse = new ProductResponse(id, productName, 1002.0, "Electronics", "Best Laptop", "url.com");
        var respString = convertToJson(productResponse);
        ProductRequest productRequest = new ProductRequest(productName, 1002.0, "Electronics", "Best Laptop", "url.com");
        var requestJson = convertToJson(productResponse);
        when(productService.updateProductById(id, productRequest)).thenReturn(productResponse);
        mockMvc.perform(put("/products/" + id).content(requestJson).contentType(APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(content().string(respString));
    }

    @Test
    void deleteProductById() throws Exception {
        var id = UUID.fromString("abccadf2-e74c-4a06-9e32-2e6d757158b2");
        when(productService.deleteProductById(id)).thenReturn(true);
        mockMvc.perform(delete("/products/" + id))
                .andExpect(status().is(200))
                .andExpect(content().string("true"));
    }

    @SneakyThrows
    private String convertToJson(Object object) {
        return mapper.writeValueAsString(object);
    }

}