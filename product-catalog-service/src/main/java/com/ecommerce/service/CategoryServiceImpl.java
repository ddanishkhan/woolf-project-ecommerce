package com.ecommerce.service;

import com.ecommerce.dto.CustomPageDTO;
import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.model.CategoryEntity;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CustomPageDTO<CategoryResponse> getCategoryList(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        var categoryEntityPage = categoryRepository.findAll(pageable);
        var res = categoryEntityPage.getContent().stream().map(CategoryResponse::from).toList();
        return new CustomPageDTO<>( res, categoryEntityPage);
    }

    @Override
    public CategoryResponse createCategory(String name) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        var entity = categoryRepository.save(category);
        return CategoryResponse.from(entity);
    }

}
