package com.example.shop.repository;

import com.example.shop.domain.Customer;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp(){
        jdbcTemplate.execute("DELETE FROM order_positions");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM customer");
    }

    @Test
    void shouldSaveAndFindCustomer(){
        Customer customer = new Customer("Lena","Müller", "lena.müller@gmail.com");

        Customer saved = customerRepository.save(customer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("Lena");
        assertThat(saved.getLastName()).isEqualTo("Müller");
        assertThat(saved.getEmail()).isEqualTo("lena.müller@gmail.com");
    }

    @Test
    void shouldFindByEmail() {
        customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));

        Optional<Customer> found = customerRepository.findByEmail("anna@example.com");

        assertThat(found).isPresent();
        assertEquals(found.get().getFirstName(), "Anna");
    }


    @Test
    void shouldReturnEmptyWhenEmailNotFound(){

        Optional<Customer> found = customerRepository.findByEmail("nicht@vorhanden.com");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldEnforceUniqueEmail() {
        customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));

        
        assertThrows(Exception.class, () -> {
            customerRepository.save(new Customer("Bob", "Smith", "anna@example.com"));
        });
    }

    @Test
    void shouldFindAllCustomers(){

        customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));
        customerRepository.save(new Customer("Bob", "Smith", "bob@example.com"));

        var customers = customerRepository.findAll();


        assertThat(customers).hasSize(2);
    }

    @Test
    void shouldDeleteCustomer() {
     
        Customer saved = customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));

        customerRepository.deleteById(saved.getId());

        assertThat(customerRepository.findById(saved.getId())).isEmpty();
    }

}