package com.example.shop.rest.controller;

import com.example.shop.rest.dto.CreateCustomerRequest;
import com.example.shop.rest.dto.CustomerResponse;
import com.example.shop.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@CrossOrigin(origins = "http://localhost:5173") // React Dev Server
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody CreateCustomerRequest request) {
        var customer = customerService.createCustomer(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomerResponse.from(customer));
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers()
                .stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        var customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(CustomerResponse.from(customer));
    }
}