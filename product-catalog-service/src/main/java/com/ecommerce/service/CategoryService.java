package com.ecommerce.service;

import com.ecommerce.common.dtos.CustomPageDTO;
import com.ecommerce.dto.response.CategoryResponse;

import java.util.UUID;

public interface CategoryService {
    CustomPageDTO<CategoryResponse> getCategoryList(Integer page, Integer size);

    CategoryResponse createCategory(String name);

    CategoryResponse updateCategory(UUID id, String name);

    void deleteCategory(UUID categoryId);
}
