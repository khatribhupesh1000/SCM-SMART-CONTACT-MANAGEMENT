# Assignment 6 SCM endpoint summary

This is the same Assignment 5 service on port 8090, not a new service.

| Method | Path | Success / behavior |
|---|---|---|
| POST | `/v1/contact-shares` | `201 Created`, `Location`, idempotent retry |
| GET | `/v1/contact-shares` | `200 OK`, filter/sort/pagination |
| GET | `/v1/contact-shares/{shareId}` | `200 OK` or `304 Not Modified` |
| PATCH | `/v1/contact-shares/{shareId}` | `200 OK`, `If-Match` required |
| DELETE | `/v1/contact-shares/{shareId}` | `204 No Content` |
| POST | `/v1/contact-shares/{shareId}/cancellation` | `200 OK`, `409` if transition is illegal |
| OPTIONS | Collection/item/cancellation resources | `204`, `Allow` and CORS headers |

Failures use `application/problem+json;charset=UTF-8` with `type`, `title`, `status`, `detail`, and `errors[]`.
