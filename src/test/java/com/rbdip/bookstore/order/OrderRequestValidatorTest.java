package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class OrderRequestValidatorTest {

    private final OrderRequestValidator validator = new OrderRequestValidator();

    @Test
    void acceptsValidRequest() {
        CreateOrderRequest request = new CreateOrderRequest(
                "Ivan Petrov", "Moscow, Lenina 1", null, "regular", null,
                List.of(new CreateOrderRequest.Item(1L, 1)));

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void rejectsNullCustomerName() {
        CreateOrderRequest request = new CreateOrderRequest(
                null, "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(1L, 1)));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerFullName is required");
    }

    @Test
    void rejectsBlankCustomerName() {
        CreateOrderRequest request = new CreateOrderRequest(
                "   ", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(1L, 1)));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerFullName is required");
    }

    @Test
    void rejectsBlankCustomerAddress() {
        CreateOrderRequest request = new CreateOrderRequest(
                "Ivan", "  ", null, "regular", null,
                List.of(new CreateOrderRequest.Item(1L, 1)));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerAddress is required");
    }

    @Test
    void rejectsNullItems() {
        CreateOrderRequest request = new CreateOrderRequest(
                "Ivan", "Moscow", null, "regular", null, null);

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order must contain at least one item");
    }

    @Test
    void rejectsEmptyItems() {
        CreateOrderRequest request = new CreateOrderRequest(
                "Ivan", "Moscow", null, "regular", null, List.of());

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order must contain at least one item");
    }

    @Test
    void defaultsMissingQuantityToOne() {
        assertThat(validator.resolveQuantity(null)).isEqualTo(1);
    }

    @Test
    void keepsExplicitQuantity() {
        assertThat(validator.resolveQuantity(7)).isEqualTo(7);
    }

    @Test
    void rejectsZeroQuantity() {
        assertThatThrownBy(() -> validator.resolveQuantity(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity must be positive");
    }

    @Test
    void rejectsNegativeQuantity() {
        assertThatThrownBy(() -> validator.resolveQuantity(-2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity must be positive");
    }
}
