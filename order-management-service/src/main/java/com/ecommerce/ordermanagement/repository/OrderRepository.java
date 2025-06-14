package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Order entity.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds all orders placed by a specific customer.
     *
     * @param customerId The ID of the customer.
     * @return A list of orders for the given customer.
     */
    List<Order> findByCustomerId(Long customerId);
}
