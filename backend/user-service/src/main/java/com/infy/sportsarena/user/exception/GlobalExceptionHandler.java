package com.infy.sportsarena.user.exception;

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
        HttpStatus status;
        if (l.contains("not found")) status = HttpStatus.NOT_FOUND;
        else if (l.contains("unauthorized")) status = HttpStatus.FORBIDDEN;
        else if (l.contains("already") || l.contains("cannot delete")) status = HttpStatus.CONFLICT;
        else status = HttpStatus.BAD_REQUEST;
        Map<String, String> body = new HashMap<>();
        body.put("message", msg);
        return new ResponseEntity<>(body, status);
    }
}
