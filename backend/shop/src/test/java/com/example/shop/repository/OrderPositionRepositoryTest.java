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

import com.example.shop.domain.Article;
import com.example.shop.domain.Customer;
import com.example.shop.domain.Order;
import com.example.shop.domain.OrderPosition;
import com.example.shop.domain.OrderStatus;

@DataJdbcTest 
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderPositionRepositoryTest {

    @Autowired 
    private OrderPositionRepository orderPositionRepository;

    @Autowired 
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM order_positions");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM article");
    }

    @Test
    void shouldSaveAndFindOrderPosition(){

    Customer customer = new Customer("Lena", "Müller", "lena.müller@gmail.com");
    Customer saved = customerRepository.save(customer);

    Order order = new Order(saved.getId(), OrderStatus.PLACED);
    Order placed = orderRepository.save(order);

    Article article = new Article("Book", new BigDecimal("5.00"));
    Article created = articleRepository.save(article);

    OrderPosition orderPosition = new OrderPosition(1, created.getPrice(), placed.getId(), created.getId());
    OrderPosition savedOP = orderPositionRepository.save(orderPosition);

    assertThat(savedOP.getId()).isNotNull();assertThat(savedOP.getQuantity()).isEqualTo(1);
    assertThat(savedOP.getPrice()).isEqualTo(new BigDecimal("5.00"));
    assertThat(savedOP.getOrderId()).isEqualTo(placed.getId());
    assertThat(savedOP.getArticleId()).isEqualTo(created.getId());
    }



    @Test
    void shouldFindByOrderId(){
        Customer customer = new Customer("Lena", "Müller", "lena.müller@gmail.com");
        Customer saved = customerRepository.save(customer);
    
        Order order = new Order(saved.getId(), OrderStatus.PLACED);
        Order placed = orderRepository.save(order);
    
        Article article = new Article("Book", new BigDecimal("5.00"));
        Article created = articleRepository.save(article);

        OrderPosition orderPosition = new OrderPosition(1, created.getPrice(), placed.getId(), created.getId());
        OrderPosition savedOP = orderPositionRepository.save(orderPosition);

        List<OrderPosition> found = orderPositionRepository.findByOrderId(placed.getId());

        assertThat(found).hasSize(1);

        }


    @Test
    void shouldReturnEmptyWhenNoPositions(){
    
        List<OrderPosition> found = orderPositionRepository.findByOrderId(999L);
        
        assertThat(found).isEmpty();
    }


    @Test
    void shouldDeleteOrderPositions(){

        Customer customer = new Customer("Lena", "Müller", "lena.müller@gmail.com");
        Customer saved = customerRepository.save(customer);
    
        Order order = new Order(saved.getId(), OrderStatus.PLACED);
        Order placed = orderRepository.save(order);
    
        Article article = new Article("Book", new BigDecimal("5.00"));
        Article created = articleRepository.save(article);

        OrderPosition orderPosition = new OrderPosition(1, created.getPrice(), placed.getId(), created.getId());
        OrderPosition savedOP = orderPositionRepository.save(orderPosition);


        orderPositionRepository.deleteById(savedOP.getId());
        
        assertThat(orderPositionRepository.findById(savedOP.getId())).isEmpty();
    }
}
