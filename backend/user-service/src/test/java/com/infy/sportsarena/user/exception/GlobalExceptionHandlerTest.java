package com.infy.sportsarena.user.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void validationErrorsReturnBadRequest() {
        BindingResult binding = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(binding);
        when(binding.getFieldErrors()).thenReturn(List.of(new FieldError("dto", "email", "invalid")));
        var response = handler.handleValidation(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("email", "invalid");
    }

    @Test
    void runtimeErrorsMapStatusesAndNullMessage() {
        assertStatus("Profile not found", HttpStatus.NOT_FOUND);
        assertStatus("Unauthorized request", HttpStatus.FORBIDDEN);
        assertStatus("Team already exists", HttpStatus.CONFLICT);
        assertStatus("Cannot delete team", HttpStatus.CONFLICT);
        assertStatus("Invalid request", HttpStatus.BAD_REQUEST);
        var response = handler.handleRuntime(new RuntimeException((String) null));
        assertThat(response.getBody()).containsEntry("message", "Unexpected error");
    }

    private void assertStatus(String message, HttpStatus status) {
        var response = handler.handleRuntime(new RuntimeException(message));
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).containsEntry("message", message);
    }
}
