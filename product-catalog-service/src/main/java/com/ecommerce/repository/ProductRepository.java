package com.ecommerce.repository;

import com.ecommerce.model.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    Page<ProductEntity> findAll(Pageable pageable);
    Optional<ProductEntity> findByName(String name);
    Optional<ProductEntity> findByNameAndDescription(String name, String description); // select * from Product where name = ? and description = ?
    Optional<ProductEntity> findByNameOrDescription(String name, String description); // select * from Product where name = ? or description = ?
    Optional<ProductEntity> findByPriceLessThanEqual(double price); // <= price
    Optional<ProductEntity> findByPriceLessThan(double price); // < price
    Optional<ProductEntity> findByPriceGreaterThanEqual(double price); // >= price
    Optional<ProductEntity> findByPriceGreaterThan(double price); // > price
    Optional<ProductEntity> findByPriceBetween(double startPrice, double endPrice);

    @Query(value = CustomQueries.FIND_PRODUCT_BY_NAME, nativeQuery = true)
    Optional<ProductEntity> findByNameContaining(String name);
}
