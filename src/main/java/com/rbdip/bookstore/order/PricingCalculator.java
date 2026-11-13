package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PricingCalculator {

    static final int BULK_QUANTITY_THRESHOLD = 10;
    static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.95");

    static final String FIXED_COUPON_CODE = "SAVE10";
    static final BigDecimal FIXED_COUPON_AMOUNT = BigDecimal.TEN;
    static final String PERCENT_COUPON_CODE = "SAVE20PERCENT";
    static final BigDecimal PERCENT_COUPON_RATE = new BigDecimal("0.8");

    static final BigDecimal LARGE_ORDER_THRESHOLD = new BigDecimal("1000");
    static final BigDecimal LARGE_ORDER_DISCOUNT_RATE = new BigDecimal("0.98");

    static final int MONEY_SCALE = 2;

    public record LineItem(BigDecimal price, int quantity) {
    }

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = BigDecimal.ZERO;
        for (LineItem item : items) {
            total = total.add(linePrice(item));
        }

        total = total.multiply(CustomerType.fromCode(customerType).getDiscountRate());
        total = applyCoupon(total, couponCode);
        total = total.max(BigDecimal.ZERO);
        total = applyLargeOrderDiscount(total);

        return total.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal linePrice(LineItem item) {
        BigDecimal price = item.price().multiply(BigDecimal.valueOf(item.quantity()));
        if (item.quantity() > BULK_QUANTITY_THRESHOLD) {
            return price.multiply(BULK_DISCOUNT_RATE);
        }
        return price;
    }

    private BigDecimal applyCoupon(BigDecimal total, String couponCode) {
        if (FIXED_COUPON_CODE.equals(couponCode)) {
            return total.subtract(FIXED_COUPON_AMOUNT);
        }
        if (PERCENT_COUPON_CODE.equals(couponCode)) {
            return total.multiply(PERCENT_COUPON_RATE);
        }
        return total;
    }

    private BigDecimal applyLargeOrderDiscount(BigDecimal total) {
        if (total.compareTo(LARGE_ORDER_THRESHOLD) > 0) {
            return total.multiply(LARGE_ORDER_DISCOUNT_RATE);
        }
        return total;
    }
}
