package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderWriter {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderWriter(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Order saveNewOrder(CreateOrderRequest request, List<OrderLine> lines) {
        Order order = orderRepository.save(new Order(
                request.customerFullName(), request.customerAddress(), request.customerPhone(), Order.STATUS_NEW));
        for (OrderLine line : lines) {
            orderItemRepository.save(line.toOrderItem(order.getId()));
        }
        return order;
    }
}
