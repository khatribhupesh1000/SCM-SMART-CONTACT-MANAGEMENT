package io.scm.shares.error;

import io.scm.shares.model.FieldViolation;
import io.scm.shares.model.Problem;
import io.scm.shares.service.ContactServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final MediaType PROBLEM_JSON =
            MediaType.parseMediaType("application/problem+json;charset=UTF-8");

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Problem> api(ApiException e) {
        if (e.getStatus() >= 500) log.warn("Mapped API failure type={} status={} detail={}", e.getType(), e.getStatus(), e.getMessage());
        return problem(e.getStatus(), e.getTitle(), e.getMessage(), e.getType(), List.of());
    }

    @ExceptionHandler(ContactServiceClient.DependencyException.class)
    public ResponseEntity<Problem> dependency(ContactServiceClient.DependencyException e) {
        log.warn("SCM Contact Service dependency failure status={}", e.getStatus());
        String type = e.getStatus() == 502 ? "contact-service-error" : "dependency-unavailable";
        return problem(e.getStatus(), e.getStatus() == 502 ? "Bad Gateway" : "Service Unavailable",
                e.getMessage(), type, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> validation(MethodArgumentNotValidException e) {
        List<FieldViolation> errors = e.getBindingResult().getFieldErrors().stream()
                .map(x -> new FieldViolation(x.getField(), safeReason(x.getDefaultMessage())))
                .distinct().toList();
        return problem(422, "Unprocessable Content",
                "One or more fields failed validation.", "invalid-share-request", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Problem> malformed(HttpMessageNotReadableException e) {
        return problem(400, "Bad Request", "Request body must contain valid JSON.",
                "invalid-request", List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Problem> methodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return problem(405, "Method Not Allowed", "The HTTP method is not supported for this resource.",
                "method-not-allowed", List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Problem> unsupportedMediaType(HttpMediaTypeNotSupportedException e) {
        return problem(415, "Unsupported Media Type", "Use Content-Type: application/json for request bodies.",
                "unsupported-media-type", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> unexpected(Exception e) {
        // Keep exception/stack trace server-side; never return DB or framework messages.
        log.error("Unhandled request failure", e);
        return problem(500, "Internal Server Error",
                "The server could not complete the request.", "internal-error", List.of());
    }

    private ResponseEntity<Problem> problem(int status, String title, String detail,
                                               String type, List<FieldViolation> errors) {
        Problem body = new Problem(type, title, status, detail, errors);
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status).contentType(PROBLEM_JSON);
        if (status == 429 || status == 503) builder.header(HttpHeaders.RETRY_AFTER, "30");
        return builder.body(body);
    }

    private String safeReason(String reason) {
        if (reason == null || reason.isBlank()) return "is invalid";
        return reason.replaceAll("[\\r\\n]", " ");
    }
}
