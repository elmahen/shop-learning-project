package com.example.shop.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("customer")
public class Customer {

    @Id
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Konstruktoren ─────────────────────────────────────────

    public Customer() {}

    public Customer(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getter / Setter ───────────────────────────────────────

    public Long getId()                   { return id; }
    public String getFirstName()          { return firstName; }
    public String getLastName()           { return lastName; }
    public String getEmail()              { return email; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }

    public void setId(Long id)                        { this.id = id; }
    public void setFirstName(String firstName)        { this.firstName = firstName; }
    public void setLastName(String lastName)          { this.lastName = lastName; }
    public void setEmail(String email)                { this.email = email; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", email='" + email + "'}";
    }
}