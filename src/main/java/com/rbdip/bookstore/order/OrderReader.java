package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderReader {

    private final OrderRepository orderRepository;

    public OrderReader(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderSummary> listOrders() {
        return orderRepository.findAllWithItems().stream()
                .map(OrderReader::toSummary)
                .toList();
    }

    private static OrderSummary toSummary(Order order) {
        List<OrderSummary.Item> items = order.getItems().stream()
                .map(item -> new OrderSummary.Item(item.getProductName(), item.getQuantity()))
                .toList();
        return new OrderSummary(order.getId(), order.getCustomerFullName(), order.getStatus(), items);
    }
}
