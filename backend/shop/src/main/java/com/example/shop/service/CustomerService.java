vpackage com.example.shop.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.domain.Customer;
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

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private OrderPositionRepository orderPositionRepository;

    @Autowired(required = false)
    private PaymentRepository paymentRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(String firstName, String lastName, String email) {
        customerRepository.findByEmail(email).ifPresent(existing -> {
            throw new CustomerAlreadyExistsException(
                "Ein Kunde mit der E-Mail: '" + email + "' existiert bereits."
            );
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

    // Use Case 9: Kunde Saldo berechnen
    public BigDecimal calculateSaldo(Long customerId) {
        getCustomerById(customerId);

        if (paymentRepository == null || orderRepository == null || orderPositionRepository == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPaid = paymentRepository.findByCustomerIdOrderByDateAsc(customerId).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOrdered = orderRepository.findByCustomerIdOrderByOrderDateAsc(customerId).stream()
                .filter(order -> order.getOrderStatus() != OrderStatus.CANCELLED)
                .flatMap(order -> orderPositionRepository.findByOrderId(order.getId()).stream())
                .map(pos -> pos.getPrice().multiply(BigDecimal.valueOf(pos.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPaid.subtract(totalOrdered);
    }
}