package com.ecommerce.repository;

public class CustomQueries {
    private CustomQueries(){}
    public static final String FIND_PRODUCT_BY_NAME = "select * from products where name like %:name%";
}
