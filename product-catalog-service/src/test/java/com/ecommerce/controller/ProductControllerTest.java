package com.ecommerce.controller;

import com.ecommerce.config.CustomJwtDecoder;
import com.ecommerce.common.dtos.CustomPageDTO;
import com.ecommerce.common.dtos.product.ProductRequest;
import com.ecommerce.common.dtos.product.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "productService")
    private ProductService productService;

    @MockitoBean
    private CustomJwtDecoder jwtDecoder;

    private final ObjectMapper mapper = new ObjectMapper();


    @Test
    @Order(1)
    @WithMockUser
    void getAllProductsReturnEmptyListWhenNoProductsAvailable() throws Exception {
        when(productService.getAllProducts(anyInt(), anyInt())).thenReturn(new CustomPageDTO<>());

        mockMvc.perform(get("/products"))
                .andExpect(status().is(200))
                .andExpect(content()
                        .json("""
                        {"products":[],"totalPages":0,"totalElements":0}
                        """, JsonCompareMode.STRICT));
    }

    @Test
    @Order(2)
    @WithMockUser
    void getAllProductsReturnProducts() throws Exception {

        ProductResponse product1 = new ProductResponse(
                UUID.fromString("feecadf2-e74c-4a06-9e32-2e6d757158b2"), "Laptop", new BigDecimal("1000.0"), "Electronics", "Best Laptop", 1, "url.com");
        List<ProductResponse> productListResponseDTO = List.of(product1);
        Page page = new PageImpl(productListResponseDTO, Pageable.ofSize(10), 1);
        var res = new CustomPageDTO<>(productListResponseDTO, page);
        when(productService.getAllProducts(anyInt(), anyInt())).thenReturn(res);

        mockMvc.perform(get("/products"))
                .andExpect(status().is(200))
                .andExpect(content().json("""
                        {"products":[{"id":"feecadf2-e74c-4a06-9e32-2e6d757158b2","name":"Laptop","price":1000.0,"category":"Electronics","description":"Best Laptop","image":"url.com"}],"totalPages":0,"totalElements":10}""", JsonCompareMode.STRICT));
    }

    @Test
    @WithMockUser
    void getProductById_Failure() throws Exception {
        UUID id = UUID.fromString("feecadf2-e74c-4a06-9e32-2e6d757158b2");
        when(productService.getProductById(id)).thenThrow(new ProductNotFoundException("Product Not Found"));
        mockMvc.perform(get("/products/" + id))
                .andExpect(status().is(404))
                .andExpect(content().json("""
                        {"message":"Product Not Found","messageCode":404}""", JsonCompareMode.STRICT));
    }

    @Test
    @WithMockUser
    void findProductByIdSuccess() throws Exception {
        var id = UUID.fromString("feecadf2-e74c-4a06-9e32-2e6d757158b2");
        var productResponse = new ProductResponse(id, "Laptop", new BigDecimal("1000.0"), "Electronics", "Best Laptop", 1,"url.com");
        var respString = convertToJson(productResponse);

        when(productService.getProductById(id)).thenReturn(productResponse);
        mockMvc.perform(get("/products/" + id))
                .andExpect(status().is(200))
                .andExpect(content().string(respString));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void addNewProduct() throws Exception {
        var id = UUID.fromString("abcdadf2-e74c-4a06-1111-2e6d757158b2");
        var categoryId = UUID.randomUUID();
        var productName = "Laptop";
        var productResponse = new ProductResponse(id, productName, new BigDecimal("1001.0"), "Electronics", "Best Laptop", 1, "url.com");
        var respString = convertToJson(productResponse);
        ProductRequest productRequest = new ProductRequest(productName, new BigDecimal("1001.0"), categoryId, "Best Laptop",1, "url.com");
        var requestJson = convertToJson(productResponse);
        when(productService.createNewProduct(productRequest)).thenReturn(productResponse);
        mockMvc.perform(post("/products").with(csrf()).content(requestJson).contentType(APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(content().json(respString));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateProductDetail() throws Exception {
        var id = UUID.fromString("abcdadf2-e74c-4a06-2222-2e6d757158b2");
        var categoryId = UUID.randomUUID();
        var productName = "Laptop";
        var productResponse = new ProductResponse(id, productName, new BigDecimal("1002.0"), "Electronics", "Best Laptop", 1, "url.com");
        var respString = convertToJson(productResponse);
        ProductRequest productRequest = new ProductRequest(productName, new BigDecimal("1002.0"), categoryId, "Best Laptop", 1, "url.com");
        var requestJson = convertToJson(productResponse);
        when(productService.updateProductById(id, productRequest)).thenReturn(productResponse);
        mockMvc.perform(put("/products/" + id).with(csrf()).content(requestJson).contentType(APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(content().string(respString));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteProductById() throws Exception {
        var id = UUID.fromString("abccadf2-e74c-4a06-9e32-2e6d757158b2");
        when(productService.deleteProductById(id)).thenReturn(true);
        mockMvc.perform(delete("/products/" + id).with(csrf()))
                .andExpect(status().is(204));
    }

    @SneakyThrows
    private String convertToJson(Object object) {
        return mapper.writeValueAsString(object);
    }

}