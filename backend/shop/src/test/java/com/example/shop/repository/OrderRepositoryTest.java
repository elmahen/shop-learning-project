package com.example.shop.repository;

import com.example.shop.domain.Customer;
import com.example.shop.domain.Order;
import com.example.shop.domain.OrderStatus;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

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
    void shouldSaveAndFindOrder() {

        Customer customer = new Customer("Lena", "Müller", "lena.müller@gmail.com");

        Customer saved = customerRepository.save(customer);

        Order order = new Order(saved.getId(), OrderStatus.PENDING);

        Order placed = orderRepository.save(order);

        assertThat(placed.getId()).isNotNull();
        assertThat(placed.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(placed.getOrderDate()).isNotNull();
        assertThat(placed.getCustomerId()).isEqualTo(customer.getId());
    }

    @Test
    void shouldFindByCustomerId() {
        Customer customer = new Customer("Lena", "Müller", "lena.müller@gmail.com");

        Customer saved = customerRepository.save(customer);

        Order order = new Order(saved.getId(), OrderStatus.PENDING);

        Order placed = orderRepository.save(order);

        List<Order> found = orderRepository.findByCustomerIdOrderByOrderDateAsc(customer.getId());

        assertThat(found).hasSize(1);

    }

    @Test
    void shouldReturnEmptyWhenNoOrders() {
        List<Order> found = orderRepository.findByCustomerIdOrderByOrderDateAsc(123L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteOrder() {

        Customer customer = new Customer("Lena", "Müller", "lena.müller@gmail.com");

        Customer saved = customerRepository.save(customer);

        Order order = new Order(saved.getId(), OrderStatus.PENDING);

        Order placed = orderRepository.save(order);

        orderRepository.deleteById(placed.getId());

        assertThat(orderRepository.findById(placed.getId())).isEmpty();
    }

}
