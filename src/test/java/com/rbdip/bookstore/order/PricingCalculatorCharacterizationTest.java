package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PricingCalculatorCharacterizationTest {

    private static final String REGULAR = "regular";
    private static final String VIP = "vip";
    private static final String WHOLESALE = "wholesale";

    private final PricingCalculator calculator = new PricingCalculator();

    @Test
    @DisplayName("Пустой заказ стоит 0.00")
    void emptyOrderCostsZero() {
        assertThat(calculate(List.of(), REGULAR, null)).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Стоимость нескольких позиций суммируется")
    void sumsSeveralLines() {
        List<PricingCalculator.LineItem> items = List.of(
                new PricingCalculator.LineItem(new BigDecimal("10.00"), 2),
                new PricingCalculator.LineItem(new BigDecimal("5.50"), 3));

        assertThat(calculate(items, REGULAR, null)).isEqualByComparingTo("36.50");
    }

    @Test
    @DisplayName("Оптовая скидка не применяется ровно на пороге (10 штук)")
    void bulkDiscountIsNotAppliedAtThreshold() {
        List<PricingCalculator.LineItem> items = List.of(
                new PricingCalculator.LineItem(new BigDecimal("10.00"), 10));

        assertThat(calculate(items, REGULAR, null)).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Оптовая скидка применяется при количестве больше порога (11 штук)")
    void bulkDiscountIsAppliedAboveThreshold() {
        List<PricingCalculator.LineItem> items = List.of(
                new PricingCalculator.LineItem(new BigDecimal("10.00"), 11));

        assertThat(calculate(items, REGULAR, null)).isEqualByComparingTo("104.50");
    }

    @Test
    @DisplayName("Оптовая скидка считается по каждой позиции отдельно")
    void bulkDiscountIsPerLine() {
        List<PricingCalculator.LineItem> items = List.of(
                new PricingCalculator.LineItem(new BigDecimal("10.00"), 11),
                new PricingCalculator.LineItem(new BigDecimal("10.00"), 10));

        assertThat(calculate(items, REGULAR, null)).isEqualByComparingTo("204.50");
    }

    @ParameterizedTest(name = "Тип клиента {0} даёт итог {1}")
    @CsvSource({
        "regular, 100.00",
        "vip, 90.00",
        "wholesale, 85.00"
    })
    void appliesCustomerDiscount(String customerType, String expected) {
        assertThat(calculate(single(new BigDecimal("100.00"), 1), customerType, null))
                .isEqualByComparingTo(expected);
    }

    @ParameterizedTest(name = "Неизвестный тип клиента \"{0}\" считается обычным")
    @ValueSource(strings = {"", "premium", "REGULAR", "unknown"})
    void unknownCustomerTypeFallsBackToRegular(String customerType) {
        assertThat(calculate(single(new BigDecimal("100.00"), 1), customerType, null))
                .isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("null вместо типа клиента считается обычным клиентом")
    void nullCustomerTypeFallsBackToRegular() {
        assertThat(calculate(single(new BigDecimal("100.00"), 1), null, null))
                .isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Фиксированный купон вычитает 10.00")
    void fixedCouponSubtractsTen() {
        assertThat(calculate(single(new BigDecimal("50.00"), 1), REGULAR, "SAVE10"))
                .isEqualByComparingTo("40.00");
    }

    @Test
    @DisplayName("Фиксированный купон не делает итог отрицательным")
    void fixedCouponNeverMakesTotalNegative() {
        assertThat(calculate(single(new BigDecimal("5.00"), 1), REGULAR, "SAVE10"))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Фиксированный купон, обнуляющий итог ровно в ноль")
    void fixedCouponCanMakeExactZero() {
        assertThat(calculate(single(new BigDecimal("10.00"), 1), REGULAR, "SAVE10"))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Процентный купон оставляет 80% суммы")
    void percentCouponKeepsEightyPercent() {
        assertThat(calculate(single(new BigDecimal("100.00"), 1), REGULAR, "SAVE20PERCENT"))
                .isEqualByComparingTo("80.00");
    }

    @Test
    @DisplayName("Купон применяется после скидки типа клиента")
    void couponIsAppliedAfterCustomerDiscount() {
        assertThat(calculate(single(new BigDecimal("100.00"), 1), VIP, "SAVE10"))
                .isEqualByComparingTo("80.00");
    }

    @ParameterizedTest(name = "Неизвестный купон \"{0}\" игнорируется")
    @ValueSource(strings = {"", "SAVE30", "save10", "SAVE20PERCENT "})
    void unknownCouponIsIgnored(String couponCode) {
        assertThat(calculate(single(new BigDecimal("100.00"), 1), REGULAR, couponCode))
                .isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("null вместо купона игнорируется")
    void nullCouponIsIgnored() {
        assertThat(calculate(single(new BigDecimal("100.00"), 1), REGULAR, null))
                .isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Скидка на крупный заказ не применяется ровно на пороге 1000")
    void largeOrderDiscountIsNotAppliedAtThreshold() {
        assertThat(calculate(single(new BigDecimal("1000.00"), 1), REGULAR, null))
                .isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Скидка 2% применяется к сумме больше 1000")
    void largeOrderDiscountIsAppliedAboveThreshold() {
        assertThat(calculate(single(new BigDecimal("1000.01"), 1), REGULAR, null))
                .isEqualByComparingTo("980.01");
    }

    @Test
    @DisplayName("Скидка на крупный заказ применяется после процентного купона")
    void largeOrderDiscountIsAppliedAfterPercentCoupon() {
        assertThat(calculate(single(new BigDecimal("2000.00"), 1), REGULAR, "SAVE20PERCENT"))
                .isEqualByComparingTo("1568.00");
    }

    @Test
    @DisplayName("Все скидки комбинируются в заданном порядке")
    void combinesBulkCustomerCouponAndLargeOrderDiscounts() {
        List<PricingCalculator.LineItem> items = List.of(
                new PricingCalculator.LineItem(new BigDecimal("100.00"), 11));

        assertThat(calculate(items, WHOLESALE, "SAVE10")).isEqualByComparingTo("878.25");
    }

    @Test
    @DisplayName("Деньги округляются до копеек по HALF_UP")
    void roundsHalfUpToTwoDecimals() {
        assertThat(calculate(single(new BigDecimal("10.005"), 1), REGULAR, null))
                .isEqualByComparingTo("10.01");
        assertThat(calculate(single(new BigDecimal("10.004"), 1), REGULAR, null))
                .isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("Округление выполняется и после скидки клиента")
    void roundsAfterCustomerDiscount() {
        assertThat(calculate(single(new BigDecimal("3.33"), 1), VIP, null))
                .isEqualByComparingTo("3.00");
    }

    @Test
    @DisplayName("Отрицательная цена позиции приводит к итогу 0.00")
    void negativePriceIsClampedToZero() {
        assertThat(calculate(single(new BigDecimal("-10.00"), 1), REGULAR, null))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Отрицательное количество приводит к итогу 0.00")
    void negativeQuantityIsClampedToZero() {
        assertThat(calculate(single(new BigDecimal("10.00"), -3), REGULAR, null))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Нулевое количество не добавляет стоимости")
    void zeroQuantityAddsNothing() {
        assertThat(calculate(single(new BigDecimal("999.99"), 0), REGULAR, null))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Нулевая цена не добавляет стоимости")
    void zeroPriceAddsNothing() {
        assertThat(calculate(single(BigDecimal.ZERO, 5), REGULAR, null))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Фиксированный купон применяется раньше скидки на крупный заказ")
    void fixedCouponIsAppliedBeforeLargeOrderDiscount() {
        assertThat(calculate(single(new BigDecimal("1009.00"), 1), REGULAR, "SAVE10"))
                .isEqualByComparingTo("999.00");
    }

    @Test
    @DisplayName("Процентный купон применяется после оптовой скидки")
    void percentCouponIsAppliedAfterBulkDiscount() {
        List<PricingCalculator.LineItem> items = List.of(
                new PricingCalculator.LineItem(new BigDecimal("100.00"), 11));

        assertThat(calculate(items, WHOLESALE, "SAVE20PERCENT")).isEqualByComparingTo("710.60");
    }

    @Test
    @DisplayName("Фиксированный купон и скидка на крупный заказ суммируются")
    void fixedCouponAndLargeOrderDiscountCombine() {
        assertThat(calculate(single(new BigDecimal("2000.00"), 1), REGULAR, "SAVE10"))
                .isEqualByComparingTo("1950.20");
    }

    @Test
    @DisplayName("Оптовая скидка на дробную цену округляется HALF_UP")
    void bulkDiscountOnFractionalPriceRoundsHalfUp() {
        assertThat(calculate(single(new BigDecimal("0.33"), 11), REGULAR, null))
                .isEqualByComparingTo("3.45");
    }

    @Test
    @DisplayName("Скидка на крупный заказ с дробным результатом округляется HALF_UP")
    void largeOrderDiscountRoundsHalfUp() {
        assertThat(calculate(single(new BigDecimal("1000.25"), 1), REGULAR, null))
                .isEqualByComparingTo("980.25");
    }

    private BigDecimal calculate(
            List<PricingCalculator.LineItem> items, String customerType, String couponCode) {
        return calculator.calculateOrderTotal(items, customerType, couponCode);
    }

    private static List<PricingCalculator.LineItem> single(BigDecimal price, int quantity) {
        return List.of(new PricingCalculator.LineItem(price, quantity));
    }
}
