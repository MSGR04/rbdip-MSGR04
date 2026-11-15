package com.rbdip.bookstore.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomerNameTest {

    @Test
    void splitsFirstTokenIntoFirstNameAndRemainderIntoLastName() {
        CustomerName name = CustomerName.parse("Ivan Petrov Sidorov");

        assertThat(name.firstName()).isEqualTo("Ivan");
        assertThat(name.lastName()).isEqualTo("Petrov Sidorov");
        assertThat(name.fullName()).isEqualTo("Ivan Petrov Sidorov");
    }

    @Test
    void keepsSingleTokenInFirstName() {
        CustomerName name = CustomerName.parse("Ivan");

        assertThat(name.firstName()).isEqualTo("Ivan");
        assertThat(name.lastName()).isEmpty();
        assertThat(name.fullName()).isEqualTo("Ivan");
    }

    @Test
    void trimsOuterAndRepeatedSpaces() {
        CustomerName name = CustomerName.parse("  Ivan   Petrov  ");

        assertThat(name.firstName()).isEqualTo("Ivan");
        assertThat(name.lastName()).isEqualTo("Petrov");
        assertThat(name.fullName()).isEqualTo("Ivan Petrov");
    }

    @Test
    void handlesNullAndBlankNames() {
        assertThat(CustomerName.parse(null).fullName()).isEmpty();
        assertThat(CustomerName.parse("   ").fullName()).isEmpty();
    }
}
