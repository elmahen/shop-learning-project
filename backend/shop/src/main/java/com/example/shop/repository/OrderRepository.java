package com.example.shop.repository;

import org.springframework.data.repository.ListCrudRepository;

import com.example.shop.domain.Order;
import java.util.List;

public interface OrderRepository extends ListCrudRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByOrderDateAsc(Long id);

    
}
