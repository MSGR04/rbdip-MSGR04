package com.rbdip.bookstore.order;

import java.math.BigDecimal;

public interface OrderNotifier {

    void notifyOrderPlaced(Order order, BigDecimal total);
}
