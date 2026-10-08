# CS543 Assignment 4 — SCM Email Service (Spring Boot)

Team 12 — Aman Bali (20251651015), Bhupesh Kumar (20251651032)

This is a Spring Boot/Java implementation of the Assignment 4 resource, OpenAPI, validation, idempotency, error-shape, testing and network-reliability requirements, adapted to the Email Service boundary defined in the team’s SCM Assignment 2.

## Endpoints

- `POST /api/v1/emails` — create/queue an email; requires `Idempotency-Key`; returns `201 Created` + `Location`.
- `GET /api/v1/emails/{emailRef}` — single resource read; `200` or `404`.
- `GET /api/v1/emails?recipient=...` — filtered list.
- `POST /api/v1/emails/{emailRef}/cancellation` — state-changing sub-resource; `200`, `404`, `409` or `422`.

## Network hardening

The service calls the existing SCM Contact API using `CONTACT_SERVICE_URL`. The call has a 1.5s timeout and up to three attempts with exponential backoff and jitter. 4xx responses are not retried.

Example:

```bash
export CONTACT_SERVICE_URL=http://localhost:8081/api/contacts
mvn spring-boot:run
```

Assignment 4 service port: `8090`.

## Submission files

- `SUBMISSION_COVER.md` — team and project details
- `openapi.yaml` — contract-first OpenAPI document
- `src/main/java/...` — Spring Boot implementation
- `src/test/java/...` — automated tests
- `NOTES.md` — A4 resource table, A5 choice, D3 fallback and all five required answers
- `curl-transcript.txt` — required curl commands/transcript template
- `validation-output.txt` — honest validation status and local validator command
- `test-output.txt` — honest test execution status and local command
