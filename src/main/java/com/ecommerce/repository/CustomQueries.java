package com.ecommerce.repository;

public interface CustomQueries {
    String FIND_PRODUCT_BY_NAME = "select * from products where name like %:name%";
}
