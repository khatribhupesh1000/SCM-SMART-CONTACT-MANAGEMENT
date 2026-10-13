# Endpoint summary

| Method | Path | Purpose |
|---|---|---|
| POST | `/v1/contact-shares` | Create a share request with an idempotency key |
| GET | `/v1/contact-shares` | List/filter share requests |
| GET | `/v1/contact-shares/{shareId}` | Read one share request |
| POST | `/v1/contact-shares/{shareId}/cancellation` | Cancel a pending request |
| PATCH | `/v1/contact-shares/{shareId}` | Conditional partial update using If-Match |
| DELETE | `/v1/contact-shares/{shareId}` | Delete a share request |
| OPTIONS | Collection/item/cancellation resources | Return Allow and CORS headers |
