package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomerTypeTest {

    @Test
    void resolvesKnownCodes() {
        assertThat(CustomerType.fromCode("regular")).isEqualTo(CustomerType.REGULAR);
        assertThat(CustomerType.fromCode("vip")).isEqualTo(CustomerType.VIP);
        assertThat(CustomerType.fromCode("wholesale")).isEqualTo(CustomerType.WHOLESALE);
    }

    @Test
    void unknownOrMissingCodeFallsBackToRegular() {
        assertThat(CustomerType.fromCode("gold")).isEqualTo(CustomerType.REGULAR);
        assertThat(CustomerType.fromCode(null)).isEqualTo(CustomerType.REGULAR);
    }

    @Test
    void exposesCodesAndDiscountRates() {
        assertThat(CustomerType.REGULAR.getCode()).isEqualTo("regular");
        assertThat(CustomerType.VIP.getCode()).isEqualTo("vip");
        assertThat(CustomerType.WHOLESALE.getCode()).isEqualTo("wholesale");
        assertThat(CustomerType.REGULAR.getDiscountRate()).isEqualByComparingTo("1.0");
        assertThat(CustomerType.VIP.getDiscountRate()).isEqualByComparingTo("0.9");
        assertThat(CustomerType.WHOLESALE.getDiscountRate()).isEqualByComparingTo("0.85");
    }
}
