package com.ecommerce.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CategoryEntity extends BaseUUIDEntity {
    private String name;
}
