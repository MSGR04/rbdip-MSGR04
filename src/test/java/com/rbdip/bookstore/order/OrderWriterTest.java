package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.customer.Customer;
import com.rbdip.bookstore.customer.CustomerRepository;
import com.rbdip.bookstore.product.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrderWriterTest {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private CustomerRepository customerRepository;
    private OrderWriter orderWriter;

    private CreateOrderRequest request;
    private Product product;
    private OrderLine line;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        customerRepository = mock(CustomerRepository.class);
        orderWriter = new OrderWriter(orderRepository, orderItemRepository, customerRepository);

        request = new CreateOrderRequest(
                "Ivan Petrov", "Moscow, Lenina 1", "+79990000000", "regular", null,
                List.of(new CreateOrderRequest.Item(1L, 2)));
        product = new Product("Clean Code", new BigDecimal("35.00"), null);
        line = new OrderLine(product, 2);
    }

    @Test
    void createsCustomerAndOrderWithItems() {
        when(customerRepository.findFirstByFullNameAndAddress("Ivan Petrov", "Moscow, Lenina 1"))
                .thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order saved = orderWriter.saveNewOrder(request, List.of(line));

        assertThat(saved.getCustomerFullName()).isEqualTo("Ivan Petrov");
        assertThat(saved.getCustomerAddress()).isEqualTo("Moscow, Lenina 1");
        assertThat(saved.getCustomerPhone()).isEqualTo("+79990000000");
        assertThat(saved.getStatus()).isEqualTo(Order.STATUS_NEW);

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository).save(itemCaptor.capture());
        OrderItem savedItem = itemCaptor.getValue();
        assertThat(savedItem.getProduct()).isSameAs(product);
        assertThat(savedItem.getQuantity()).isEqualTo(2);
        assertThat(savedItem.getOrderId()).isEqualTo(saved.getId());
    }

    @Test
    void reusesExistingCustomerAndRefreshesContactDetails() {
        Customer existing = new Customer("Ivan Petrov", "Moscow, Lenina 1", null);
        when(customerRepository.findFirstByFullNameAndAddress("Ivan Petrov", "Moscow, Lenina 1"))
                .thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order saved = orderWriter.saveNewOrder(request, List.of(line));

        assertThat(saved.getCustomer()).isSameAs(existing);
        assertThat(existing.getAddress()).isEqualTo("Moscow, Lenina 1");
        assertThat(existing.getPhone()).isEqualTo("+79990000000");
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
