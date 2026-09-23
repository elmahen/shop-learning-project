package com.example.shop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.domain.Customer;
import com.example.shop.domain.Order;
import com.example.shop.domain.OrderStatus;
import com.example.shop.domain.Payment;
import com.example.shop.exception.CustomerAlreadyExistsException;
import com.example.shop.exception.CustomerNotFoundException;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderPositionRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.PaymentRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderPositionRepository orderPositionRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderPositionRepository orderPositionRepository, PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderPositionRepository = orderPositionRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Customer createCustomer(
            String firstName,
            String lastName,
            String email) {

        customerRepository.findByEmail(email).ifPresent(existing -> {
            throw new CustomerAlreadyExistsException(
                    "Ein Kunde mit der E-Mail: '" + email
                            + "' existiert bereits.");
        });

        Customer customer = new Customer(firstName, lastName, email);
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Kunde nicht gefunden"));
    }

    public void notifyOverdueCustomers() {
        List<Customer> customers = customerRepository.findAll();

        for (Customer customer : customers) {
            List<Payment> payments = paymentRepository.findByCustomerIdOrderByDateAsc(customer.getId());
            BigDecimal totalPaid = payments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateAsc(customer.getId());
            if (orders.isEmpty())
                continue;

            BigDecimal totalOrdered = orders.stream()
                    .filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED)
                    .flatMap(o -> orderPositionRepository.findByOrderId(o.getId()).stream())
                    .map(op -> op.getPrice().multiply(new BigDecimal(op.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldo = totalPaid.subtract(totalOrdered);

            Order lastOrder = orders.get(orders.size() - 1);
            LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

            if (saldo.compareTo(BigDecimal.ZERO) < 0 && lastOrder.getOrderDate().isBefore(threeMonthsAgo)) {
                System.out.println("WARNUNG: Kunde " + customer.getEmail() + " hat offenen Saldo: " + saldo);
            }
        }
    }

}