package com.example.shop.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.example.shop.domain.Payment;

public interface PaymentRepository extends ListCrudRepository<Payment, Long> {

    List<Payment> findByCustomerIdOrderByDateAsc(Long customerId);

}
