package com.example.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.shop.domain.Customer;
import com.example.shop.exception.CustomerAlreadyExistsException;
import com.example.shop.exception.CustomerNotFoundException;
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

        assertThrows(CustomerAlreadyExistsException.class,
                () -> customerService.createCustomer("Bob", "Smith", "anna@example.com"));
    }

    @Test
    void shouldGetCustomerById() {
        Customer customer = new Customer("Anna", "Müller", "anna@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById(1L);

        assertThat(result.getEmail()).isEqualTo("anna@example.com");
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(999L));
    }

    @Test
    void shouldGetAllCustomers() {
        when(customerRepository.findAll()).thenReturn(List.of(
                new Customer("Anna", "Müller", "anna@example.com"),
                new Customer("Bob", "Smith", "bob@example.com")));

        var result = customerService.getAllCustomers();

        assertThat(result).hasSize(2);
    }
}