package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRequestValidator validator;
    private final OrderLineResolver lineResolver;
    private final PricingCalculator pricingCalculator;
    private final OrderWriter orderWriter;
    private final OrderNotifier notifier;

    public OrderService(
            OrderRequestValidator validator,
            OrderLineResolver lineResolver,
            PricingCalculator pricingCalculator,
            OrderWriter orderWriter,
            OrderNotifier notifier) {
        this.validator = validator;
        this.lineResolver = lineResolver;
        this.pricingCalculator = pricingCalculator;
        this.orderWriter = orderWriter;
        this.notifier = notifier;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        validator.validate(request);
        List<OrderLine> lines = lineResolver.resolve(request.items());

        BigDecimal total = calculateTotal(request, lines);
        Order order = orderWriter.saveNewOrder(request, lines);
        notifier.notifyOrderPlaced(order, total);

        return order;
    }

    private BigDecimal calculateTotal(CreateOrderRequest request, List<OrderLine> lines) {
        List<PricingCalculator.LineItem> lineItems = lines.stream()
                .map(OrderLine::toLineItem)
                .toList();
        return pricingCalculator.calculateOrderTotal(lineItems, request.customerType(), request.couponCode());
    }
}
