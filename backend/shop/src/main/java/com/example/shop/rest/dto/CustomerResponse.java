package com.example.shop.rest.dto;

import com.example.shop.domain.Customer;

public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    // Statische Factory-Methode: Entity → DTO
    public static CustomerResponse from(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.id        = customer.getId();
        response.firstName = customer.getFirstName();
        response.lastName  = customer.getLastName();
        response.email     = customer.getEmail();
        return response;
    }

    public Long getId()          { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getEmail()     { return email; }
}