package com.rbdip.bookstore.customer;

public record CustomerName(String firstName, String lastName) {

    public CustomerName(String firstName, String lastName) {
        this.firstName = normalize(firstName);
        this.lastName = normalize(lastName);
    }

    public static CustomerName parse(String fullName) {
        String normalized = fullName == null ? "" : fullName.strip();
        if (normalized.isEmpty()) {
            return new CustomerName("", "");
        }
        int separator = normalized.indexOf(' ');
        if (separator < 0) {
            return new CustomerName(normalized, "");
        }
        return new CustomerName(
                normalized.substring(0, separator),
                normalized.substring(separator + 1).strip());
    }

    public String fullName() {
        return (firstName + " " + lastName).strip();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.strip();
    }
}
