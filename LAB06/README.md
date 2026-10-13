# SCM Assignment 6 — Spring Boot

Team 12 — Aman Bali (20251651015), Bhupesh Kumar (20251651032)

Assignment 6 updates the same Assignment 5 SCM contact-share service. It does not introduce a new microservice or port.

## Stack
Java 21 · Spring Boot 3.2.5 · Spring Web · Jakarta Bean Validation · Maven · JUnit 5 · Mockito · MockMvc

## Ports
- SCM Contact Service: `8082`
- Existing Assignment 5/6 contact-share service: `8090`

## Run
```bash
export CONTACT_SERVICE_URL="http://localhost:8082/v1/contacts"
mvn clean test
mvn spring-boot:run
```

Assignment 6 adds a unified `application/problem+json;charset=UTF-8` error format, stable SCM error types, field-level 422 errors, Retry-After for 429/503, consistent response charset, content negotiation (JSON or 406 for unsupported XML), safer internal-error mapping, and updated OpenAPI documentation.

See `NOTES.md` for the status map, error catalogue, validation rules, retry policy, and answers to the seven assignment questions. `transcript.txt` is a checklist. Capture live evidence with `capture-curl-transcript.sh` before submitting.
