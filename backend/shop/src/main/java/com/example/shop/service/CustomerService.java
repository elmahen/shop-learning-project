package com.example.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.domain.Customer;
import com.example.shop.exception.CustomerAlreadyExistsException;
import com.example.shop.exception.CustomerNotFoundException;
import com.example.shop.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(
            String firstName,
            String lastName,
            String email) {

        customerRepository.findByEmail(email).ifPresent(existing -> {
            throw new CustomerAlreadyExistsException(
                    "Ein Kunde mit der E-Mail: '" + email
                            + "' existiert bereits.");
        });

        Customer customer = new Customer(firstName, lastName, email);
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Kunde nicht gefunden"));
    }

}