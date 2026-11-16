package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderReaderTest {

    private OrderRepository orderRepository;
    private OrderReader orderReader;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderReader = new OrderReader(orderRepository);
    }

    @Test
    void mapsOrderAndItsItemsToSummary() {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(1L);
        when(order.getCustomerFullName()).thenReturn("Anna Sidorova");
        when(order.getStatus()).thenReturn(Order.STATUS_NEW);

        OrderItem item = mock(OrderItem.class);
        when(item.getProductName()).thenReturn("Refactoring");
        when(item.getQuantity()).thenReturn(2);
        when(order.getItems()).thenReturn(List.of(item));

        when(orderRepository.findAllWithItems()).thenReturn(List.of(order));

        List<OrderSummary> summaries = orderReader.listOrders();

        assertThat(summaries).containsExactly(
                new OrderSummary(1L, "Anna Sidorova", "new",
                        List.of(new OrderSummary.Item("Refactoring", 2))));
    }

    @Test
    void returnsEmptyListWhenThereAreNoOrders() {
        when(orderRepository.findAllWithItems()).thenReturn(List.of());

        assertThat(orderReader.listOrders()).isEmpty();
    }
}
