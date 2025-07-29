package com.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class CategoryEntity extends BaseUUIDEntity {
    @Column(unique = true, nullable = false)
    private String name;
}
