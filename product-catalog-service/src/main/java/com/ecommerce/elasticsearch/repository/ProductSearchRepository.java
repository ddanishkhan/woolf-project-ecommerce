package com.ecommerce.elasticsearch.repository;


import com.ecommerce.elasticsearch.model.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    // Find products by name containing a string (case-insensitive)
    List<ProductDocument> findByNameContainingIgnoreCase(String name);

    List<ProductDocument> findByCategory(String category);

    // Find products by name OR description
    List<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    /**
     * Performs a multi-match fuzzy search across the name and description fields.
     * The '?0' placeholder is replaced by the first method parameter (query).
     *
     * @param query The search term to find.
     * @param pageable Pagination information.
     * @return A page of matching product documents.
     */
    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": [
                  "name",
                  "description"
                ],
                "fuzziness": "AUTO"
              }
            }""")
    Page<ProductDocument> searchFuzzyInNameAndDescription(String query, Pageable pageable);
}