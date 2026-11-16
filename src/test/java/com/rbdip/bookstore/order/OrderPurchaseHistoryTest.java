package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderPurchaseHistoryTest {

    private OrderItemRepository orderItemRepository;
    private OrderPurchaseHistory purchaseHistory;

    @BeforeEach
    void setUp() {
        orderItemRepository = mock(OrderItemRepository.class);
        purchaseHistory = new OrderPurchaseHistory(orderItemRepository);
    }

    @Test
    void reportsPurchaseWhenProductWasOrdered() {
        when(orderItemRepository.existsByProductId(7L)).thenReturn(true);

        assertThat(purchaseHistory.hasPurchased(7L)).isTrue();
    }

    @Test
    void reportsNoPurchaseWhenProductWasNeverOrdered() {
        when(orderItemRepository.existsByProductId(8L)).thenReturn(false);

        assertThat(purchaseHistory.hasPurchased(8L)).isFalse();
    }
}
