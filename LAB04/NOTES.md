# CS543 Assignment 4 — SCM Email Service (Spring Boot)

## Important domain choice
The supplied Assignment 4 brief is written for CampusEats and asks for Catalogue, Orders or Delivery. The previous Assignment 2 files for this team were explicitly adapted to **Smart Contact Manager (SCM)**, with Identity, Contact, Media & Storage, Email and Feedback services. This implementation therefore keeps the Assignment 4 engineering requirements but applies them to the **Email Service boundary from the team's previous SCM design**, rather than inventing a new boundary. The Email Service owns email requests, recipients/attachments and email idempotency state; it calls the Contact Service through a contract rather than reading Contact data directly.

## A2 boundary reused
Previous SCM Assignment 2 defined the Email Service operation `sendEmail(senderRef, recipientEmail, subject, bodyText, attachmentRef?, idempotencyKey?)` and errors including RecipientInvalid, AttachmentTooLarge, DeliveryFailed and IdempotencyConflict. This REST implementation preserves that ownership while representing the durable noun as `emails`.

## A4 resource table

| Method | URL | What it does | Success | Failure |
|---|---|---|---|---|
| POST | `/api/v1/emails` | Creates/queues an email request for a contact | 201 + Location | 400, 422, 503 |
| GET | `/api/v1/emails/{emailRef}` | Reads one email | 200 | 404 |
| GET | `/api/v1/emails?recipient=...` | Lists emails filtered by resolved recipient email | 200 | 400 |
| POST | `/api/v1/emails/{emailRef}/cancellation` | Changes a queued email into cancelled state | 200 | 400, 404, 409, 422 |

## A5 hard choice
The least comfortable operation was cancellation because `cancelEmail` is an action-shaped name in the old contract. I represented the durable resource as `emails` and the state transition as the sub-resource `/emails/{emailRef}/cancellation`. I rejected `/cancelEmail` because the brief requires resource-shaped URLs and rejects verbs in URLs. Because this implementation completes the state transition immediately, it returns 200 rather than falsely claiming asynchronous work with 202.

## B contract
`openapi.yaml` is the contract-first artifact. Schemas are declared once under `components.schemas` and referenced with `$ref`. Every endpoint documents its success and failure responses. The public representation intentionally differs from the stored record: the model contains an internal UUID while `asJson()` exposes only the public `emailRef` and contract fields.

## C implementation
- Spring Boot 3.2.5 / Java 21.
- Standalone Assignment 4 service, port 8090.
- In-process `EmailStore` is allowed by the brief.
- `CreateEmailRequest.validate()` is called before request fields are used.
- `GlobalExceptionHandler` produces one `Problem(type,title,status,detail)` shape for failures.
- POST creation requires `Idempotency-Key` and stores the key with the resulting email reference. A repeated key returns the original representation without creating another record.
- Four required endpoint types are implemented: create, single read, filtered list, and state-changing sub-resource.

## D1/D2/D3 cross-service call
The Email Service resolves a contact's current email address by making a real HTTP GET to the SCM Contact Service. The address is read only from `CONTACT_SERVICE_URL`; no production URL is hard-coded.

Example when the existing SCM app is running on 8081:

```bash
export CONTACT_SERVICE_URL=http://localhost:8081/api/contacts
```

The outbound GET has a 1.5 second timeout. Transient failures are retried up to three attempts using exponential backoff and jitter. A 4xx response is never retried. A 404 from Contact is mapped to 422 `RECIPIENT_UNAVAILABLE`; a dependency/network failure is mapped to 503 `DEPENDENCY_UNAVAILABLE`.

### Fallback
The service deliberately fails with 503 when the Contact Service is unavailable. Degrading would be wrong because the Email Service cannot safely determine the recipient address from its own data without violating the service boundary defined in Assignment 2. Returning 201 without resolving the recipient would create an email request whose destination was not verified.

## Assignment 3 comparison
The previous SCM Assignment 3 used QuickPing's SOAP 1.1 `SendSms` partner. Its WSDL declared the `SendSms` operation, SOAP binding, message structure, authentication header and typed `SmsFault`; the partner fault codes included `INVALID_MSISDN`, `RATE_LIMITED`, `INSUFFICIENT_CREDIT`, `GATEWAY_UNAVAILABLE` and `AUTH_FAILED`.

### 1. WSDL vs OpenAPI line count
`partner.wsdl` is 149 lines and this `openapi.yaml` is 183 lines. The difference is 34 lines. The difference is not a measure of complexity by itself: WSDL carries XML Schema types plus SOAP messages, portType, binding, SOAPAction and service/port/address information. Two things the WSDL declares that this OpenAPI document does not need are the SOAP binding/SOAPAction and the WSDL message/portType/service structure. HTTP methods, URLs and JSON schemas are enough for this REST contract.

### 2. SOAP fault replaced by HTTP status + Problem
Assignment 3's `soap-fault.xml` contains the typed fault code `INVALID_MSISDN` and marks it non-retryable. The previous design mapped it to HTTP 422 with an SCM error such as `CONTACT_PHONE_INVALID`. In this Assignment 4 service, the same principle is used: the transport status communicates failure and the JSON Problem body gives machine-readable detail. Returning an application error inside HTTP 200 is harmful because intermediaries, monitoring systems, clients and retry logic can interpret 200 as success and skip normal failure handling.

### 3. UDDI publish/find/bind
The old setup used a lightweight catalogue instead of a live UDDI server. In REST, the service URL is configured directly through `CONTACT_SERVICE_URL`, while OpenAPI supplies the contract. Publish/find/bind as a UDDI protocol is therefore reduced: deployment/configuration provides the endpoint, and OpenAPI replaces much of the contract-discovery role. There is no UDDI runtime call in this implementation.

### 4. XML Schema vs Java validation
The function carrying the manual validation responsibility is `CreateEmailRequest.validate()`. It checks senderRef, contactRef, subject, bodyText and attachmentRef before the service uses them. Without it, for example, an empty subject or body over the allowed 5000-character limit could enter the service even though the OpenAPI schema documents those constraints.

### 5. Where SOAP would still be preferable
For the external QuickPing SMS gateway, SOAP remains a reasonable choice when the partner contract is already WSDL/SOAP and the typed message/fault contract plus message-level authentication semantics are required. The guarantee being bought is interoperability against a formal XML contract with explicit typed faults and a defined SOAP binding, rather than simply choosing SOAP because it is older.

## Run

Terminal 1 — existing SCM Contact Service:

```bash
# Your existing SCM Spring Boot application
# expected: http://localhost:8081
```

Terminal 2 — Assignment 4:

```bash
export CONTACT_SERVICE_URL=http://localhost:8081/api/contacts
./mvnw spring-boot:run
```

The Assignment 4 service is then available at `http://localhost:8090`.

If your existing SCM uses another contact endpoint, change only `CONTACT_SERVICE_URL`; the Assignment 4 code does not hard-code it.
