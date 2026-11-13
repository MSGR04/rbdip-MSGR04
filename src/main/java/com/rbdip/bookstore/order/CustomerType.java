package com.rbdip.bookstore.order;

import java.math.BigDecimal;

public enum CustomerType {

    REGULAR("regular", BigDecimal.ONE),
    VIP("vip", new BigDecimal("0.9")),
    WHOLESALE("wholesale", new BigDecimal("0.85"));

    private final String code;
    private final BigDecimal discountRate;

    CustomerType(String code, BigDecimal discountRate) {
        this.code = code;
        this.discountRate = discountRate;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public static CustomerType fromCode(String code) {
        for (CustomerType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return REGULAR;
    }
}
