package com.ecommerce.service;

import com.ecommerce.dto.CustomPageDTO;
import com.ecommerce.dto.response.CategoryResponse;

public interface CategoryService {
    CustomPageDTO<CategoryResponse> getCategoryList(Integer page, Integer size);

    CategoryResponse createCategory(String name);
}
