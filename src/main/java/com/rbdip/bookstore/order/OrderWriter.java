package com.rbdip.bookstore.order;

import com.rbdip.bookstore.customer.Customer;
import com.rbdip.bookstore.customer.CustomerRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderWriter {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;

    public OrderWriter(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
    }

    public Order saveNewOrder(CreateOrderRequest request, List<OrderLine> lines) {
        Customer customer = findOrCreateCustomer(request);
        Order order = orderRepository.save(new Order(customer, Order.STATUS_NEW));
        for (OrderLine line : lines) {
            orderItemRepository.save(line.toOrderItem(order.getId()));
        }
        return order;
    }

    private Customer findOrCreateCustomer(CreateOrderRequest request) {
        return customerRepository.findFirstByFullNameAndAddress(
                        request.customerFullName(), request.customerAddress())
                .map(customer -> updateContactDetails(customer, request))
                .orElseGet(() -> customerRepository.save(new Customer(
                        request.customerFullName(), request.customerAddress(), request.customerPhone())));
    }

    private Customer updateContactDetails(Customer customer, CreateOrderRequest request) {
        customer.updateContactDetails(request.customerAddress(), request.customerPhone());
        return customer;
    }
}
