package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Order entity.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds all orders placed by a specific customer.
     *
     * @param customerId The ID of the customer.
     * @param pageable
     * @return A list of orders for the given customer.
     */
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
}
