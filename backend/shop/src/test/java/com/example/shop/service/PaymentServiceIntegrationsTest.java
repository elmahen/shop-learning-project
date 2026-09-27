package com.example.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.shop.domain.Article;
import com.example.shop.domain.Customer;
import com.example.shop.domain.Order;
import com.example.shop.domain.OrderPosition;
import com.example.shop.domain.OrderStatus;
import com.example.shop.repository.ArticleRepository;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderPositionRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.PaymentRepository;

@SpringBootTest
class PaymentServiceIntegrationTest {

    @Autowired private PaymentService paymentService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderPositionRepository orderPositionRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM order_positions");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM article");
    }

    @Test
    void shouldMarkOrderAsPaidWhenFullyPaid() {
        Customer customer = customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));
        Article article = articleRepository.save(new Article("Book", new BigDecimal("50.00")));
        Order order = orderRepository.save(new Order(customer.getId(), OrderStatus.PLACED));
        orderPositionRepository.save(new OrderPosition(1, new BigDecimal("50.00"), order.getId(), article.getId()));

        paymentService.processPayment(customer.getId(), new BigDecimal("50.00"));

        Order updated = orderRepository.findById(order.getId()).get();
        assertThat(updated.getOrderStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldNotMarkOrderAsPaidWhenPartiallyPaid() {
        Customer customer = customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));
        Article article = articleRepository.save(new Article("Book", new BigDecimal("100.00")));
        Order order = orderRepository.save(new Order(customer.getId(), OrderStatus.PLACED));
        orderPositionRepository.save(new OrderPosition(1, new BigDecimal("100.00"), order.getId(), article.getId()));

        paymentService.processPayment(customer.getId(), new BigDecimal("50.00"));

        Order updated = orderRepository.findById(order.getId()).get();
        assertThat(updated.getOrderStatus()).isEqualTo(OrderStatus.PLACED);
    }

    @Test
    void shouldThrowWhenAmountIsInvalid() {
        Customer customer = customerRepository.save(new Customer("Anna", "Müller", "anna@example.com"));

        assertThrows(IllegalArgumentException.class, () ->
            paymentService.processPayment(customer.getId(), BigDecimal.ZERO)
        );

        assertThat(paymentRepository.findByCustomerIdOrderByDateAsc(customer.getId())).isEmpty();
    }
}