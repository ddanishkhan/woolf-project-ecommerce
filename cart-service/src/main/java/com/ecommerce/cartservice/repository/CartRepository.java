package com.ecommerce.cartservice.repository;

import com.ecommerce.cartservice.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {

    /**
     * Finds a cart by the user's ID.
     * The userId is expected to be unique (e.g., user's email).
     * @param userId The unique identifier for the user.
     * @return An Optional containing the Cart if found.
     */
    Optional<Cart> findByUserId(String userId);
}