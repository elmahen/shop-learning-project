package com.example.shop.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.example.shop.domain.OrderPosition;

public interface OrderPositionRepository extends ListCrudRepository<OrderPosition, Long> {

    List<OrderPosition> findByOrderId(Long orderId);
}
