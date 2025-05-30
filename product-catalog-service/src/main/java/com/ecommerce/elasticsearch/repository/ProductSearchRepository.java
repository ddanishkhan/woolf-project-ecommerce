package com.ecommerce.elasticsearch.repository;


import com.ecommerce.elasticsearch.model.ProductDocument;
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

    // For more complex queries, use @Query annotation with Elasticsearch Query DSL
    // @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": \"?0\"}}, {\"match\": {\"category\": \"?1\"}}]}}")
    // List<ProductDocument> findByNameAndCategory(String name, String category);
}