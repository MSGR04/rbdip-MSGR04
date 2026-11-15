package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.customer.Customer;
import com.rbdip.bookstore.product.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderModelTest {

    @Test
    void orderExposesCustomerDetailsAndDefaults() {
        Customer customer = new Customer("Anna", "Sidorova", "SPb, Nevsky 10", "+79991112233");
        Order order = new Order(customer, Order.STATUS_NEW);

        assertThat(order.getCustomer()).isSameAs(customer);
        assertThat(order.getCustomerFullName()).isEqualTo("Anna Sidorova");
        assertThat(order.getCustomerAddress()).isEqualTo("SPb, Nevsky 10");
        assertThat(order.getCustomerPhone()).isEqualTo("+79991112233");
        assertThat(order.getStatus()).isEqualTo("new");
        assertThat(order.getCreatedAt()).isNotNull();
    }

    @Test
    void customerExposesFieldsAndCanBeUpdated() {
        Customer customer = new Customer("Ivan", "Petrov", "Moscow", null);

        assertThat(customer.getFirstName()).isEqualTo("Ivan");
        assertThat(customer.getLastName()).isEqualTo("Petrov");
        assertThat(customer.getFullName()).isEqualTo("Ivan Petrov");
        assertThat(customer.getAddress()).isEqualTo("Moscow");
        assertThat(customer.getPhone()).isNull();

        customer.updateContactDetails("Kazan", "+79990000000");

        assertThat(customer.getAddress()).isEqualTo("Kazan");
        assertThat(customer.getPhone()).isEqualTo("+79990000000");
    }

    @Test
    void orderItemExposesProductData() {
        Product product = new Product("Refactoring", new BigDecimal("40.00"), "book");
        OrderItem item = new OrderItem(7L, product, 3);

        assertThat(item.getOrderId()).isEqualTo(7L);
        assertThat(item.getProduct()).isSameAs(product);
        assertThat(item.getProductName()).isEqualTo("Refactoring");
        assertThat(item.getProductPrice()).isEqualByComparingTo("40.00");
        assertThat(item.getQuantity()).isEqualTo(3);
    }

    @Test
    void orderLineBuildsPricingItemAndOrderItem() {
        Product product = new Product("Clean Code", new BigDecimal("35.00"), "book");
        OrderLine line = new OrderLine(product, 2);

        PricingCalculator.LineItem pricingItem = line.toLineItem();
        assertThat(pricingItem.price()).isEqualByComparingTo("35.00");
        assertThat(pricingItem.quantity()).isEqualTo(2);

        OrderItem orderItem = line.toOrderItem(11L);
        assertThat(orderItem.getOrderId()).isEqualTo(11L);
        assertThat(orderItem.getProduct()).isSameAs(product);
        assertThat(orderItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void productExposesFields() {
        Product product = new Product("Domain-Driven Design", new BigDecimal("50.00"), "book");

        assertThat(product.getName()).isEqualTo("Domain-Driven Design");
        assertThat(product.getPrice()).isEqualByComparingTo("50.00");
        assertThat(product.getDescription()).isEqualTo("book");
        assertThat(product.getId()).isNull();
    }

    @Test
    void loggingNotifierAcceptsOrderAndTotal() {
        Order order = new Order(new Customer("Ivan", "Petrov", "Moscow", null), Order.STATUS_NEW);

        new LoggingEmailNotifier().notifyOrderPlaced(order, new BigDecimal("70.00"));
    }
}
