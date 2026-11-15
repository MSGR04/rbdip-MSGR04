package com.rbdip.bookstore;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void validationErrorBecomesBadRequestWithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleValidation(new IllegalArgumentException("quantity must be positive"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "quantity must be positive");
    }
}
