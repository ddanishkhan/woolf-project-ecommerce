package com.ecommerce.elasticsearch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "products") // Name of the Elasticsearch index
public class ProductDocument {

    @Id
    private String id; // Elasticsearch ID, can be same as your ProductEntity UUID

    @Field(type = FieldType.Text, name = "name")
    private String name;

    @Field(type = FieldType.Text, name = "description")
    private String description;

    @Field(type = FieldType.Double, name = "price")
    private Double price;

    @Field(type = FieldType.Keyword, name = "category") // Keyword for exact matches, Text for full-text search
    private String category;

    @Field(type = FieldType.Integer, name = "stockQuantity") // Keyword for exact matches, Text for full-text search
    private Integer stockQuantity;

    @Field(type = FieldType.Text, name = "imageURL")
    private String imageURL;

    // Helper method to convert from ProductEntity (JPA) to ProductDocument (Elasticsearch)
    public static ProductDocument fromProductEntity(com.ecommerce.model.ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductDocument.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .category(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .imageURL(entity.getCoverImageURL())
                .build();
    }
}