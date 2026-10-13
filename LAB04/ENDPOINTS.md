# Endpoint summary

| Method | Path | Purpose |
|---|---|---|
| POST | `/v1/contact-shares` | Create a share request with an idempotency key |
| GET | `/v1/contact-shares` | List/filter share requests |
| GET | `/v1/contact-shares/{shareId}` | Read one share request |
| POST | `/v1/contact-shares/{shareId}/cancellation` | Cancel a pending request |
