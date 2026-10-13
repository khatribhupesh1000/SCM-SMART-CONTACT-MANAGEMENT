# SCM Assignment 2 — aligned to the actual project

**Team ID:** 12  
**Aman Bali — 20251651015**  
**Bhupesh Kumar — 20251651032**

This corrected package supersedes the earlier draft where it contradicted the uploaded `SERVICE_BASED` repository by splitting relational persistence into several databases and presenting proposed media/notification tables as deployed schema.

## Actual service boundaries from `SERVICE_BASED/README.md`

| Service | Port | Owns / provides |
|---|---:|---|
| API Gateway | 8080 | Express edge, routes, validation, OpenAPI/Swagger, GraphQL |
| Auth Service | 8081 | Authentication, user identity, roles and OAuth |
| Contact Service | 8082 | Contact CRUD, search, favorite filtering, pagination |
| Media Service | 8083 | Contact photos/media |
| Notification Service | 8084 | Asynchronous email |
| Audit Service | 8085 | MongoDB audit documents, contact notes and activity |
| Integration Service | 8086 | SOAP/WSDL integration, timeout/retry/circuit breaker |

## Schema correction

`schema.sql` now follows the actual migration at `SERVICE_BASED/database/mysql/001_initial_schema.sql`: `users`, `contact`, and `idempotency_keys` in the same relational schema. MongoDB collections are documented separately rather than invented as SQL tables. The actual entity source uses `contacts` for the JPA entity table in Contact Service; the supplied migration file names its table `contact`. Check the configured database and migration state before running any schema changes, because repository artifacts show this naming difference.

This is a design/submission schema aligned to the checked-in migration, not a claim that it has been applied to your live database.
