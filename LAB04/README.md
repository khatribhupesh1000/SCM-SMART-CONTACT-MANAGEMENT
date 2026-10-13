# SCM Assignment 4 — Spring Boot

**Team ID:** 12  
**Aman Bali — 20251651015**  
**Bhupesh Kumar — 20251651032**

This service is written in Java 21 and Spring Boot 3.2.5, not Python. It is designed as a sibling service for the existing Smart Contact Manager project.

## Ports
- Existing SCM Contact Service: `8082`
- This assignment's contact-share service: `8090`

## Run
Make sure Java 21 and Maven are installed. Start your SCM Contact Service first, then run this service:

```bash
export CONTACT_SERVICE_URL="http://localhost:8082/v1/contacts"
mvn clean test
mvn spring-boot:run
```

The assignment 5 version uses a demonstration header token:
```bash
curl -i -H 'Authorization: Bearer scm-assignment5-demo-token' \
  -H 'Accept: application/json' \
  'http://localhost:8090/v1/contact-shares'
```

Assignment 4 has four required resource operations. Assignment 5 extends the service with OPTIONS/Allow, method and header policies, list sorting/pagination, ETag conditional reads and writes, DELETE, CORS, per-client rate limiting, and gzip compression.

**Important:** the in-memory store is for this assignment. The `CONTACT_SERVICE_URL` must point at the actual Contact Service endpoint and a real contact ID is needed for successful create tests. The sample bearer token is a header-handling exercise, not production authentication. See `NOTES.md` and `VALIDATION.md` for submission requirements.
