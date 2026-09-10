package com.example.shop.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.shop.domain.Customer;
import com.example.shop.domain.Payment;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM order_positions");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM customer");
    }

    @Test
    void shouldSaveAndFindPayment() {
        Customer customer = new Customer("Lena", "Müller", "lena@example.com");
        Customer saved = customerRepository.save(customer);

        Payment payment = new Payment(new BigDecimal("50.00"), saved.getId());
        Payment savedPayment = paymentRepository.save(payment);

        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getAmount()).isEqualTo(new BigDecimal("50.00"));
        assertThat(savedPayment.getCustomerId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldFindByCustomerId() {
        Customer customer = new Customer("Lena", "Müller", "lena@example.com");
        Customer saved = customerRepository.save(customer);

        paymentRepository.save(new Payment(new BigDecimal("50.00"), saved.getId()));

        List<Payment> found = paymentRepository.findByCustomerIdOrderByDateAsc(saved.getId());

        assertThat(found).hasSize(1);
    }

    @Test
    void shouldReturnEmptyWhenNoPayments() {
        List<Payment> found = paymentRepository.findByCustomerIdOrderByDateAsc(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeletePayment() {
        Customer customer = new Customer("Lena", "Müller", "lena@example.com");
        Customer saved = customerRepository.save(customer);

        Payment payment = paymentRepository.save(new Payment(new BigDecimal("50.00"), saved.getId()));

        paymentRepository.deleteById(payment.getId());

        assertThat(paymentRepository.findById(payment.getId())).isEmpty();
    }
}
