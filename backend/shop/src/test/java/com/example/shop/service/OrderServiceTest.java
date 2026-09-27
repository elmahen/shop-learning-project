package com.example.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.shop.domain.Article;
import com.example.shop.domain.Customer;
import com.example.shop.domain.Order;
import com.example.shop.domain.OrderPosition;
import com.example.shop.domain.OrderStatus;
import com.example.shop.exception.CustomerNotFoundException;
import com.example.shop.exception.OrderNotModifiableException;
import com.example.shop.repository.ArticleRepository;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderPositionRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderPositionRepository orderPositionRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer("Anna", "Müller", "anna@example.com")));
        when(orderRepository.save(any())).thenReturn(new Order(1L, OrderStatus.PENDING));

        Order result = orderService.createOrder(1L);

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any());
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> orderService.createOrder(999L));
    }

    @Test
    void shouldCancelOrder() {
        Order order = new Order(1L, OrderStatus.PLACED);
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        Order result = orderService.cancelOrder(1L);

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldThrowWhenCancellingPaidOrder() {
        Order order = new Order(1L, OrderStatus.PAID);
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderNotModifiableException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void shouldAddItemToOrder() {
        Order order = new Order(1L, OrderStatus.PENDING);
        order.setId(1L);
        Article article = new Article("Book", new BigDecimal("10.00"));
        article.setId(1L);
        OrderPosition savedPosition = new OrderPosition(1, new BigDecimal("10.00"), 1L, 1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(orderPositionRepository.save(any())).thenReturn(savedPosition);

        OrderPosition result = orderService.addItemToOrder(1L, 1L, 1);

        assertThat(result.getPrice()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    void shouldThrowWhenAddingToNonPendingOrder() {
        Order order = new Order(1L, OrderStatus.PLACED);
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderNotModifiableException.class, () -> orderService.addItemToOrder(1L, 1L, 1));
    }
}