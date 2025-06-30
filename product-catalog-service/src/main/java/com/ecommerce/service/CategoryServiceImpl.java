package com.ecommerce.service;

import com.ecommerce.dto.CustomPageDTO;
import com.ecommerce.dto.response.CategoryResponse;
import com.ecommerce.model.CategoryEntity;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
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

    @Override
    public CategoryResponse updateCategory(UUID id, String name) {
        var category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("category does not exist: " + id));
        category.setName(name);
        var entity = categoryRepository.save(category);
        return CategoryResponse.from(entity);
    }

    @Override
    public void deleteCategory(UUID id) {
        var category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("category does not exist: " + id));
        try {
            categoryRepository.delete(category);
        } catch (Exception e) {
            log.error("Error deleting category ", e);
            throw new RuntimeException("Cannot delete category, currently in use.");
        }
    }

}
