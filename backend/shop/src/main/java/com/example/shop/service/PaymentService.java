package com.example.shop.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.domain.Customer;
import com.example.shop.domain.OrderPosition;
import com.example.shop.domain.OrderStatus;
import com.example.shop.domain.Payment;
import com.example.shop.exception.CustomerNotFoundException;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderPositionRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.PaymentRepository;
import com.example.shop.domain.Order;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;

    private final OrderPositionRepository orderPositionRepository;

    private final CustomerRepository customerRepository;

    private final PaymentRepository paymentRepository;

    public PaymentService(OrderRepository orderRepository, OrderPositionRepository orderPositionRepository,
            CustomerRepository customerRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.orderPositionRepository = orderPositionRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment processPayment(Long customerId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Der Zahlungsbetrag muss grösser als 0 sein.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Kunde nicht gefunden"));

        Payment payment = new Payment(amount, customer.getId());
        Payment savedPayment = paymentRepository.save(payment);

        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateAsc(customerId);

        // Verrechnung nach FIFO
        BigDecimal totalPaid = paymentRepository.findByCustomerIdOrderByDateAsc(customerId)
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Order order : orders) {
            if (order.getOrderStatus() != OrderStatus.PLACED) {
                continue;
            }

            BigDecimal orderTotal = calculateOrderTotal(order.getId());

            if (totalPaid.compareTo(orderTotal) >= 0) {
                if (order.getOrderStatus() != OrderStatus.PAID) {
                    order.setOrderStatus(OrderStatus.PAID);
                    orderRepository.save(order);
                }
                totalPaid = totalPaid.subtract(orderTotal);
            } else {
                break;
            }
        }
        return savedPayment;
    }

    private BigDecimal calculateOrderTotal(Long orderId) {
        List<OrderPosition> positions = orderPositionRepository.findByOrderId(orderId);
        return positions.stream()
                .map(pos -> pos.getPrice().multiply(BigDecimal.valueOf(pos.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
