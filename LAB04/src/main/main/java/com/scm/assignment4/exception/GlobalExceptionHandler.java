package com.scm.assignment4.exception;

import com.scm.assignment4.dto.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<Problem> problem(int status, String title, String code, String detail) {
        return ResponseEntity.status(status).body(new Problem("https://scm.local/problems/" + code.toLowerCase(), title, status, detail));
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Problem> api(ApiException e) { return problem(e.getStatus(), e.getTitle(), e.getCode(), e.getMessage()); }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Problem> validation(IllegalArgumentException e) { return problem(422, "Validation failed", "INVALID_INPUT", e.getMessage()); }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Problem> malformed(HttpMessageNotReadableException e) { return problem(400, "Malformed request", "MALFORMED_JSON", "The request body is not valid JSON."); }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Problem> generic(Exception e) { return problem(500, "Internal server error", "INTERNAL_ERROR", "An unexpected error occurred."); }
}
