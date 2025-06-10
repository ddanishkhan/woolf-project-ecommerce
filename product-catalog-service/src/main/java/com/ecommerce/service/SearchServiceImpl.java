package com.ecommerce.service;

import com.ecommerce.dto.CustomPageDTO;
import com.ecommerce.dto.mapper.EntityToResponseMapper;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.elasticsearch.model.ProductDocument;
import com.ecommerce.elasticsearch.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{

    private final ProductSearchRepository productSearchRepository;

    // --- Elasticsearch Search Methods ---
    @Override
    public List<ProductResponse> searchProductsByName(String name) {
        log.debug("Searching products by name in Elasticsearch: {}", name);
        List<ProductDocument> documents = productSearchRepository.findByNameContainingIgnoreCase(name);
        return documents.stream()
                .map(EntityToResponseMapper::toProductResponse) // Reusing existing mapper
                .toList();
    }

    @Override
    public List<ProductResponse> searchProductsByCategory(String category) {
        log.debug("Searching products by category in Elasticsearch: {}", category);
        List<ProductDocument> documents = productSearchRepository.findByCategory(category);
        return documents.stream()
                .map(EntityToResponseMapper::toProductResponse)
                .toList();
    }

    /**
     * This is a simple OR search on name and description.
     **/
    @Override
    public List<ProductResponse> searchProductsByKeyword(String keyword) {
        log.debug("Searching products by keyword in Elasticsearch: {}", keyword);
        List<ProductDocument> documents = productSearchRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        return documents.stream()
                .map(EntityToResponseMapper::toProductResponse)
                .toList();
    }

    @Override
    public CustomPageDTO<ProductResponse> fuzzySearchInProductNameAndDescription(String query, Pageable pageable) {
        Page<ProductDocument> productDocuments = productSearchRepository.searchFuzzyInNameAndDescription(query, pageable);
        Page<ProductResponse> productResponse = productDocuments.map(EntityToResponseMapper::toProductResponse);
        return new CustomPageDTO<>(productResponse.getContent(), productDocuments);
    }

}
