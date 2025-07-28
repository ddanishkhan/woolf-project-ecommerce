package com.ecommerce.service;

import com.ecommerce.common.dtos.CustomPageDTO;
import com.ecommerce.common.dtos.product.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductSearchService {
    // for Elasticsearch search
    List<ProductResponse> searchProductsByName(String name);
    List<ProductResponse> searchProductsByCategory(String category);
    List<ProductResponse> searchProductsByNameOrDescriptionByKeyword(String keyword);

    /**
     * Searches for products using a fuzzy query for typo tolerance.
     *
     * @param query The search term.
     * @param pageable Pagination information.
     * @return A page of product responses.
     */
    CustomPageDTO<ProductResponse> fuzzySearchInProductNameAndDescription(String query, Pageable pageable);
}
