package com.dko.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String userFriendlyMessage = "An unexpected error occurred. Please try again later.";

        if (message.contains("Email already registered") || message.contains("Invalid registration")) {
            status = HttpStatus.BAD_REQUEST;
            userFriendlyMessage = message;
        } else if (message.contains("Invalid credentials") || message.contains("token has been revoked")
                || message.contains("token has expired") || message.contains("Invalid refresh token")) {
            status = HttpStatus.UNAUTHORIZED;
            userFriendlyMessage = message;
        }

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            System.err.println("❌ Unhandled RuntimeException: " + message);
            ex.printStackTrace();
        } else {
            System.out.println("ℹ️ Handled business exception: " + message + " (" + status + ")");
        }

        Map<String, String> error = new HashMap<>();
        error.put("error", status.getReasonPhrase());
        error.put("message", userFriendlyMessage);

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        System.err.println("❌ Database Integrity Violation: " + ex.getMessage());

        Map<String, String> error = new HashMap<>();
        error.put("error", "Database Error");
        error.put("message", "A database error occurred. This might be due to a duplicate entry or invalid data.");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        System.err.println("❌ Unhandled Exception: " + ex.getMessage());
        ex.printStackTrace();

        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "A server error occurred. Please try again later.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
