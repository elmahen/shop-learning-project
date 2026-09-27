package com.example.shop.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.domain.Article;
import com.example.shop.domain.Order;
import com.example.shop.domain.OrderPosition;
import com.example.shop.domain.OrderStatus;
import com.example.shop.domain.Payment;
import com.example.shop.exception.ArticleNotFoundException;
import com.example.shop.exception.CustomerBlockedException;
import com.example.shop.exception.CustomerNotFoundException;
import com.example.shop.exception.OrderNotFoundException;
import com.example.shop.exception.OrderNotModifiableException;
import com.example.shop.exception.OrderPositionNotFoundException;
import com.example.shop.repository.ArticleRepository;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderPositionRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.PaymentRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderPositionRepository orderPositionRepository;
    private final ArticleRepository articleRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final BalanceService balanceService;

    public OrderService(OrderRepository orderRepository,
            OrderPositionRepository orderPositionRepository,
            ArticleRepository articleRepository,
            CustomerRepository customerRepository,
            PaymentRepository paymentRepository,
            BalanceService balanceService) {
        this.orderRepository = orderRepository;
        this.orderPositionRepository = orderPositionRepository;
        this.articleRepository = articleRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.balanceService = balanceService;
    }

    public Order createOrder(Long customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Kunde nicht gefunden"));

        Order order = new Order(customerId, OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    public OrderPosition addItemToOrder(Long orderId, Long articleId, int quantity) {

        if (quantity <= 0)
            throw new IllegalArgumentException("Menge muss grösser als 0 sein");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Bestellung nicht gefunden"));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new OrderNotModifiableException("Bestellung kann nicht mehr bearbeitet werden");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("Artikel existiert nicht"));

        OrderPosition orderPosition = new OrderPosition(quantity, article.getPrice(), orderId,
                articleId);
        return orderPositionRepository.save(orderPosition);
    }

    public void removeItemFromOrder(Long orderId, Long itemId) {

        OrderPosition orderPosition = orderPositionRepository.findById(itemId)
                .orElseThrow(() -> new OrderPositionNotFoundException("OrderPosition nicht gefunden"));

        if (!orderPosition.getOrderId().equals(orderId)) {
            throw new OrderPositionNotFoundException("Item gehört nicht zu dieser Bestellung");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Bestellung nicht gefunden"));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new OrderNotModifiableException("Bestellung kann nicht mehr bearbeitet werden");
        }

        orderPositionRepository.deleteById(itemId);
    }

    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Bestellung nicht gefunden"));

        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.PAID) {
            throw new OrderNotModifiableException("Bestellung kann nicht mehr bearbeitet werden");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order placeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Bestellung nicht gefunden"));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new OrderNotModifiableException("Bestellung kann nicht mehr bearbeitet werden");
        }

        Long customerId = order.getCustomerId();

        BigDecimal saldo = balanceService.calculateSaldo(customerId);

        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateAsc(customerId);
        
        Optional<Order> oldestPlacedOrder = orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.PLACED)
                .findFirst();

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        if (saldo.compareTo(BigDecimal.ZERO) < 0 && oldestPlacedOrder.isPresent()
                && oldestPlacedOrder.get().getOrderDate().isBefore(threeMonthsAgo)) {
            throw new CustomerBlockedException("Kunde ist gesperrt wegen offenem Saldo");
        }
        order.setOrderStatus(OrderStatus.PLACED);
        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Bestellung nicht gefunden"));
    }
}