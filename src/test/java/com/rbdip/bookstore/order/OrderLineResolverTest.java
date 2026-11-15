package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderLineResolverTest {

    private ProductRepository productRepository;
    private OrderRequestValidator validator;
    private OrderLineResolver resolver;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        validator = mock(OrderRequestValidator.class);
        resolver = new OrderLineResolver(productRepository, validator);
    }

    @Test
    void resolvesProductsAndQuantitiesIntoLines() {
        Product product = new Product("Refactoring", new BigDecimal("40.00"), null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(validator.resolveQuantity(3)).thenReturn(3);

        List<OrderLine> lines = resolver.resolve(
                List.of(new CreateOrderRequest.Item(1L, 3)));

        assertThat(lines).containsExactly(new OrderLine(product, 3));
    }

    @Test
    void defaultsQuantityThroughValidator() {
        Product product = new Product("Refactoring", new BigDecimal("40.00"), null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(validator.resolveQuantity(null)).thenReturn(1);

        List<OrderLine> lines = resolver.resolve(
                List.of(new CreateOrderRequest.Item(1L, null)));

        assertThat(lines).containsExactly(new OrderLine(product, 1));
    }

    @Test
    void failsWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolve(List.of(new CreateOrderRequest.Item(99L, 1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("product 99 not found");
    }
}
