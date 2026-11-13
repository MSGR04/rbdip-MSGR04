package com.rbdip.bookstore.order;

import org.springframework.stereotype.Component;

@Component
public class OrderRequestValidator {

    static final int DEFAULT_QUANTITY = 1;

    public void validate(CreateOrderRequest request) {
        requireNotBlank(request.customerFullName(), "customerFullName");
        requireNotBlank(request.customerAddress(), "customerAddress");
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
    }

    public int resolveQuantity(Integer quantity) {
        int resolved = quantity == null ? DEFAULT_QUANTITY : quantity;
        if (resolved <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        return resolved;
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
