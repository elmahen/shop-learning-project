package com.example.shop.repository;

import com.example.shop.domain.Customer;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface CustomerRepository extends ListCrudRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

}