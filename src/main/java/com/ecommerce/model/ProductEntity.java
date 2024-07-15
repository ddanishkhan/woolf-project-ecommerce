package com.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ProductEntity extends BaseUUIDEntity {
    private String name;
    private String description;
    private double price;

    @ManyToOne
    private CategoryEntity category;
}
