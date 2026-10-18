package com.infy.sportsarena.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage() == null ? "Unexpected error" : ex.getMessage();
        String l = msg.toLowerCase();
        HttpStatus s;
        if (l.contains("not found")) s = HttpStatus.NOT_FOUND;
        else if (l.contains("unauthorized")) s = HttpStatus.FORBIDDEN;
        else if (l.contains("already") || l.contains("invalid status transition") ||
         l.contains("only pending")) s = HttpStatus.CONFLICT;
        else s = HttpStatus.BAD_REQUEST;
        Map<String, String> b = new HashMap<>();
        b.put("message", msg);
        return new ResponseEntity<>(b, s);
    }
}
