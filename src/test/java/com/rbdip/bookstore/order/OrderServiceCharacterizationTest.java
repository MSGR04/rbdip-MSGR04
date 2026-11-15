package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.customer.Customer;
import com.rbdip.bookstore.product.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class OrderServiceCharacterizationTest {

    private OrderRequestValidator validator;
    private OrderLineResolver lineResolver;
    private PricingCalculator pricingCalculator;
    private OrderWriter orderWriter;
    private OrderNotifier notifier;
    private OrderService orderService;

    private CreateOrderRequest request;
    private Product product;
    private OrderLine line;
    private Order order;

    @BeforeEach
    void setUp() {
        validator = mock(OrderRequestValidator.class);
        lineResolver = mock(OrderLineResolver.class);
        pricingCalculator = mock(PricingCalculator.class);
        orderWriter = mock(OrderWriter.class);
        notifier = mock(OrderNotifier.class);
        orderService = new OrderService(validator, lineResolver, pricingCalculator, orderWriter, notifier);

        product = new Product("Clean Code", new BigDecimal("35.00"), "book");
        request = new CreateOrderRequest(
                "Ivan Petrov", "Moscow, Lenina 1", "+79990000000", "vip", "SAVE10",
                List.of(new CreateOrderRequest.Item(1L, 2)));
        line = new OrderLine(product, 2);
        order = new Order(new Customer("Ivan Petrov", "Moscow, Lenina 1", "+79990000000"), Order.STATUS_NEW);
    }

    @Test
    void createsOrderAndNotifiesAboutCalculatedTotal() {
        when(lineResolver.resolve(request.items())).thenReturn(List.of(line));
        when(pricingCalculator.calculateOrderTotal(anyList(), eq("vip"), eq("SAVE10")))
                .thenReturn(new BigDecimal("60.00"));
        when(orderWriter.saveNewOrder(request, List.of(line))).thenReturn(order);

        Order created = orderService.createOrder(request);

        assertThat(created).isSameAs(order);
        InOrder calls = inOrder(validator, lineResolver, pricingCalculator, orderWriter, notifier);
        calls.verify(validator).validate(request);
        calls.verify(lineResolver).resolve(request.items());
        calls.verify(pricingCalculator).calculateOrderTotal(anyList(), eq("vip"), eq("SAVE10"));
        calls.verify(orderWriter).saveNewOrder(request, List.of(line));
        calls.verify(notifier).notifyOrderPlaced(order, new BigDecimal("60.00"));
    }

    @Test
    void convertsResolvedLinesToPricingLineItems() {
        when(lineResolver.resolve(request.items())).thenReturn(List.of(line));
        when(pricingCalculator.calculateOrderTotal(anyList(), eq("vip"), eq("SAVE10")))
                .thenReturn(new BigDecimal("60.00"));
        when(orderWriter.saveNewOrder(request, List.of(line))).thenReturn(order);

        orderService.createOrder(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PricingCalculator.LineItem>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(pricingCalculator)
                .calculateOrderTotal(captor.capture(), eq("vip"), eq("SAVE10"));

        assertThat(captor.getValue()).containsExactly(
                new PricingCalculator.LineItem(new BigDecimal("35.00"), 2));
    }

    @Test
    void propagatesValidationFailureWithoutWritingOrder() {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("order must contain at least one item"))
                .when(validator).validate(request);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");

        org.mockito.Mockito.verifyNoInteractions(lineResolver, pricingCalculator, orderWriter, notifier);
    }
}
