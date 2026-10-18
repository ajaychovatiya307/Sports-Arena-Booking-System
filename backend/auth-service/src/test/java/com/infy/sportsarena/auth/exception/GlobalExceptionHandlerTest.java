package com.infy.sportsarena.auth.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationReturnsFieldErrorsWithBadRequest() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("registerRequest", "email", "Email is invalid")));

        ResponseEntity<Map<String, String>> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("email", "Email is invalid");
    }

    @Test
    void handleRuntimeMapsKnownMessagesToExpectedStatuses() {
        assertRuntimeStatus("Customer not found", HttpStatus.NOT_FOUND);
        assertRuntimeStatus("Invalid email or password", HttpStatus.UNAUTHORIZED);
        assertRuntimeStatus("Unauthorized request", HttpStatus.FORBIDDEN);
        assertRuntimeStatus("Email already registered", HttpStatus.CONFLICT);
        assertRuntimeStatus("Invalid request", HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleRuntimeUsesFallbackForNullMessage() {
        ResponseEntity<Map<String, String>> response = handler.handleRuntime(new RuntimeException((String) null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Unexpected error");
    }

    private void assertRuntimeStatus(String message, HttpStatus expectedStatus) {
        ResponseEntity<Map<String, String>> response = handler.handleRuntime(new RuntimeException(message));

        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).containsEntry("message", message);
    }
}
