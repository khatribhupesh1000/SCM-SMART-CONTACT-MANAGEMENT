package io.scm.shares.error;

import io.scm.shares.model.Problem;
import io.scm.shares.service.ContactServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Problem> api(ApiException e) {
        return response(e.getStatus(), e.getTitle(), e.getMessage());
    }
    @ExceptionHandler(ContactServiceClient.DependencyException.class)
    public ResponseEntity<Problem> dependency(ContactServiceClient.DependencyException e) {
        return response(e.getStatus(), e.getStatus() == 502 ? "Bad Gateway" : "Service Unavailable", e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> validation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(x -> x.getField() + ": " + x.getDefaultMessage()).orElse("Request validation failed.");
        return response(400, "Bad Request", detail);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Problem> malformed(HttpMessageNotReadableException e) {
        return response(400, "Bad Request", "Request body must contain valid JSON.");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> unexpected(Exception e) {
        return response(500, "Internal Server Error", "An unexpected server error occurred.");
    }
    private ResponseEntity<Problem> response(int status, String title, String detail) {
        Problem p = new Problem("about:blank", title, status, detail);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(p);
    }
}
