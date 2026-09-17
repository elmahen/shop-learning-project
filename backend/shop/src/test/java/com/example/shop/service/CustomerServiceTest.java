package com.example.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.example.shop.domain.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.shop.domain.Customer;
import com.example.shop.exception.CustomerAlreadyExistsException;
import com.example.shop.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() {
        when(customerRepository.findByEmail("anna@example.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenReturn(new Customer("Anna", "Müller", "anna@example.com"));

        Customer result = customerService.createCustomer("Anna", "Müller", "anna@example.com");

        assertThat(result.getFirstName()).isEqualTo("Anna");
        verify(customerRepository).save(any());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        when(customerRepository.findByEmail("anna@example.com"))
            .thenReturn(Optional.of(new Customer("Anna", "Müller", "anna@example.com")));

        assertThrows(CustomerAlreadyExistsException.class, () ->
            customerService.createCustomer("Bob", "Smith", "anna@example.com")
        );
    }
}