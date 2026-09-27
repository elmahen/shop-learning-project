package com.example.shop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.domain.Customer;
import com.example.shop.domain.Order;
import com.example.shop.exception.CustomerAlreadyExistsException;
import com.example.shop.exception.CustomerNotFoundException;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;


@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final BalanceService balanceService;
    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository, BalanceService balanceService) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.balanceService = balanceService;
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
            List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateAsc(customer.getId());
            if (orders.isEmpty()) continue;
    
            BigDecimal saldo = balanceService.calculateSaldo(customer.getId());
    
            Order lastOrder = orders.get(orders.size() - 1);
            LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
    
            if (saldo.compareTo(BigDecimal.ZERO) < 0 && lastOrder.getOrderDate().isBefore(threeMonthsAgo)) {
                log.warn("Kunde {} hat offenen Saldo: {}", customer.getEmail(), saldo);            }
        }
    }

}