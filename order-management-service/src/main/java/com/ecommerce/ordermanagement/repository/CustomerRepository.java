package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Customer entity.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}

