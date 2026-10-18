package com.infy.sportsarena.booking.exception;

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
        when(binding.getFieldErrors()).thenReturn(List.of(new FieldError("dto", "timeSlot", "required")));
        var response = handler.handleValidation(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("timeSlot", "required");
    }

    @Test
    void runtimeErrorsMapToExpectedStatuses() {
        assertStatus("Booking not found", HttpStatus.NOT_FOUND);
        assertStatus("Unauthorized to view", HttpStatus.FORBIDDEN);
        assertStatus("Rating already submitted", HttpStatus.CONFLICT);
        assertStatus("Invalid status transition", HttpStatus.CONFLICT);
        assertStatus("Only PENDING bookings can be cancelled", HttpStatus.CONFLICT);
        assertStatus("Invalid request", HttpStatus.BAD_REQUEST);
    }

    @Test
    void nullRuntimeMessageUsesFallback() {
        var response = handler.handleRuntime(new RuntimeException((String) null));
        assertThat(response.getBody()).containsEntry("message", "Unexpected error");
    }

    private void assertStatus(String message, HttpStatus status) {
        var response = handler.handleRuntime(new RuntimeException(message));
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).containsEntry("message", message);
    }
}
