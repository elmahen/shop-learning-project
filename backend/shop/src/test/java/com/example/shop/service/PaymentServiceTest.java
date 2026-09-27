package com.example.shop.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.shop.domain.Customer;
import com.example.shop.domain.Payment;
import com.example.shop.exception.CustomerNotFoundException;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderPositionRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderPositionRepository orderPositionRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldProcessPayment() {
        Customer customer = new Customer("Anna", "Müller", "anna@example.com");
        customer.setId(1L);
        Payment savedPayment = new Payment(new BigDecimal("50.00"), 1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(paymentRepository.save(any())).thenReturn(savedPayment);
        when(paymentRepository.findByCustomerIdOrderByDateAsc(1L)).thenReturn(List.of(savedPayment));
        when(orderRepository.findByCustomerIdOrderByOrderDateAsc(1L)).thenReturn(List.of());

        Payment result = paymentService.processPayment(1L, new BigDecimal("50.00"));

        assertThat(result.getAmount()).isEqualTo(new BigDecimal("50.00"));
        verify(paymentRepository).save(any());
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () ->
            paymentService.processPayment(999L, new BigDecimal("50.00"))
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
            paymentService.processPayment(1L, BigDecimal.ZERO)
        );
    }
}