package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseUUIDEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(Types.LONGVARCHAR)
    private UUID id;

}
