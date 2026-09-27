package com.example.shop.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.shop.domain.Order;
import com.example.shop.domain.OrderStatus;
import com.example.shop.domain.Payment;
import com.example.shop.repository.OrderPositionRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.PaymentRepository;

@Service 
public class BalanceService {


    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderPositionRepository orderPositionRepository;

    public BalanceService(PaymentRepository paymentRepository, OrderRepository orderRepository, OrderPositionRepository orderPositionRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderPositionRepository = orderPositionRepository;
    }

    public BigDecimal calculateSaldo(Long customerId) {
        
    List<Payment> payments = paymentRepository.findByCustomerIdOrderByDateAsc(customerId);
            BigDecimal totalPaid = payments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateAsc(customerId);
        BigDecimal totalOrdered = orders.stream()
                    .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                    .flatMap(o -> orderPositionRepository.findByOrderId(o.getId()).stream())
                    .map(op -> op.getPrice().multiply(new BigDecimal(op.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldo = totalPaid.subtract(totalOrdered);

            return saldo;
    }
}
