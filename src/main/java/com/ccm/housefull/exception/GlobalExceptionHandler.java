
package com.ccm.housefull.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// Handles exceptions from controllers and converts them into clear API responses.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Invalid requests, such as attempting to hold an unavailable seat,
    // should return HTTP 400 instead of an unexplained HTTP 500.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }

    // Database errors should not be reported as ordinary validation errors.
    // Return a generic message without exposing database internals.
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseError(
            DataAccessException exception) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error",
                        "A database error occurred. Please try again."
                ));
    }
}
