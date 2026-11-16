package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderControllerTest {

    private OrderService orderService;
    private OrderReader orderReader;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        orderReader = mock(OrderReader.class);
        controller = new OrderController(orderService, orderReader);
    }

    @Test
    void createOrderReturnsIdAndStatus() {
        CreateOrderRequest request = mock(CreateOrderRequest.class);
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(42L);
        when(order.getStatus()).thenReturn("new");
        when(orderService.createOrder(request)).thenReturn(order);

        Map<String, Object> response = controller.createOrder(request);

        assertThat(response).containsEntry("id", 42L).containsEntry("status", "new");
    }

    @Test
    void listOrdersReturnsSummariesFromReader() {
        OrderSummary summary = new OrderSummary(
                1L, "Anna Sidorova", "new", List.of(new OrderSummary.Item("Refactoring", 2)));
        when(orderReader.listOrders()).thenReturn(List.of(summary));

        List<OrderSummary> response = controller.listOrders();

        assertThat(response).containsExactly(summary);
    }

    @Test
    void listOrdersReturnsEmptyListWhenThereAreNoOrders() {
        when(orderReader.listOrders()).thenReturn(List.of());

        assertThat(controller.listOrders()).isEmpty();
    }
}
