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
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        controller = new OrderController(orderService, orderRepository, orderItemRepository);
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
    void listOrdersReturnsCustomerNameAndItems() {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(1L);
        when(order.getCustomerFullName()).thenReturn("Anna Sidorova");
        when(order.getStatus()).thenReturn("new");

        OrderItem item = mock(OrderItem.class);
        when(item.getProductName()).thenReturn("Refactoring");
        when(item.getQuantity()).thenReturn(2);

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));

        List<Map<String, Object>> response = controller.listOrders();

        assertThat(response).hasSize(1);
        Map<String, Object> orderView = response.get(0);
        assertThat(orderView)
                .containsEntry("id", 1L)
                .containsEntry("customerFullName", "Anna Sidorova")
                .containsEntry("status", "new");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderView.get("items");
        assertThat(items).containsExactly(
                Map.of("productName", "Refactoring", "quantity", 2));
    }

    @Test
    void listOrdersReturnsEmptyListWhenThereAreNoOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());

        assertThat(controller.listOrders()).isEmpty();
    }
}
