# HTTP Request and Response Log — Smart Contact Manager (SCM)

## Purpose

This document records five HTTP request and response pairs
performed using `curl -i`.

The requests were made against the JSONPlaceholder public
read-only JSON API to understand HTTP requests, responses,
status codes, headers, and JSON response bodies.

---

## Request 1 — Get Post 1

### Request

```bash
curl -i https://jsonplaceholder.typicode.com/posts/1

### respanse

HTTP/2 200
date: Tue, 06 Oct 2026 09:50:18 GMT
content-type: application/json; charset=utf-8
content-length: 292
access-control-allow-credentials: true
cache-control: max-age=43200
etag: W/"124-yiKdLzqO5gfBrJFrcdJ8Yq0LGnU"
expires: -1
pragma: no-cache
vary: Origin, Accept-Encoding
x-content-type-options: nosniff
x-powered-by: Express
x-ratelimit-limit: 3600000
x-ratelimit-remaining: 3599999
x-ratelimit-reset: 1790812826
age: 28471
accept-ranges: bytes
cf-cache-status: HIT
server: cloudflare
cf-ray: a463c2d82bcfd531-LHR
alt-svc: h3=":443"; ma=86400

{
  "userId": 1,
  "id": 1,
  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
  "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
}

Annotation
- Status: 200 — The request was successful.
- Content-Type: application/json — The response contains JSON data.

### Request 2 — Get User 1

curl -i https://jsonplaceholder.typicode.com/users/1

### Response

HTTP/2 200 
date: Tue, 06 Oct 2026 09:50:30 GMT
content-type: application/json; charset=utf-8
content-length: 509
access-control-allow-credentials: true
cache-control: max-age=43200
etag: W/"1fd-+2Y3G3w049iSZtw5t1mzSnunngE"
expires: -1
pragma: no-cache
vary: Origin, Accept-Encoding
x-content-type-options: nosniff
x-powered-by: Express
x-ratelimit-limit: 3600000
x-ratelimit-remaining: 3599998
x-ratelimit-reset: 1790812826
age: 10025
accept-ranges: bytes
cf-cache-status: HIT
report-to: {"group":"cf-nel","max_age":604800,"endpoints":[{"url":"https://a.nel.cloudflare.com/report/v4?s=3%2FT5hIMjTZbE7h4yhgBbwmp9SXK0G2bEiol3juQRMKJGar2Vf2it5N9pB5KGO0FKd%2BMow94xXNKDCv4uALPAeVC9mrJYnuuLw4apW4RCjDMBwcpvFNepgmodlF4rIcerNQJBxv0ptDyckRP7M%2FN%2BiLz0nPxDtcaXgnip"}]}
nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
server: cloudflare
cf-ray: a463c31dcba7ce41-SIN
alt-svc: h3=":443"; ma=86400

{
  "id": 1,
  "name": "Leanne Graham",
  "username": "Bret",
  "email": "Sincere@april.biz",
  "address": {
    "street": "Kulas Light",
    "suite": "Apt. 556",
    "city": "Gwenborough",
    "zipcode": "92998-3874",
    "geo": {
      "lat": "-37.3159",
      "lng": "81.1496"
    }
  {
  "phone": "1-770-736-8031 x56442",
  "website": "hildegard.org",
  "company": {
    "name": "Romaguera-Crona",
    "catchPhrase": "Multi-layered client-server neural-net",
    "bs": "harness real-time e-markets"
  }


Annotation
- Status: 200 — The request was successful.
- Content-Type: application/json — The response contains JSON data.

### Request 3 — Get Comment 1

curl -i https://jsonplaceholder.typicode.com/comments/1

### Response

HTTP/2 200
date: Tue, 06 Oct 2026 09:50:40 GMT
content-type: application/json; charset=utf-8
content-length: 268
access-control-allow-credentials: true
cache-control: max-age=43200
etag: W/"10c-KJ4I9RM/+33TKdV8CFsIvqsDSP0"
expires: -1
pragma: no-cache
vary: Origin, Accept-Encoding
x-content-type-options: nosniff
x-powered-by: Express
x-ratelimit-limit: 3600000
x-ratelimit-remaining: 3599980
x-ratelimit-reset: 1790812826
accept-ranges: bytes
nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
server: cloudflare
cf-ray: a463c361ae3c71a8-HKG
alt-svc: h3=":443"; ma=86400

{
  "postId": 1,
  "id": 1,
  "name": "id labore ex et quam laborum",
  "email": "Eliseo@gardner.biz",
  "body": "laudantium enim quasi est quidem magnam voluptate ipsam eos\ntempora quo necessitatibus\ndolor quam autem quasi\nreiciendis et nam sapiente accusantium"
}

Annotation
- Status: 200 — The request was successful.
- Content-Type: application/json — The response contains JSON data.

### Request 4 — Get Comments of Post 1

curl -i https://jsonplaceholder.typicode.com/posts/1/comments

### Response

HTTP/2 200
date: Tue, 06 Oct 2026 09:50:46 GMT
content-type: application/json; charset=utf-8
cache-control: max-age=43200
etag: W/"5e6-4bSPS5tq8F8ZDeFJULWh6upjp7U"
expires: -1
pragma: no-cache
vary: Origin, Accept-Encoding
x-content-type-options: nosniff
x-powered-by: Express
x-ratelimit-limit: 3600000
x-ratelimit-remaining: 3599999
x-ratelimit-reset: 1790812826
age: 10028
cf-cache-status: HIT
server: cloudflare
cf-ray: a463c383cd77fdfc-SIN
alt-svc: h3=":443"; ma=86400

[
  {
    "postId": 1,
    "id": 1,
    "name": "id labore ex et quam laborum",
    "email": "Eliseo@gardner.biz",
    "body": "laudantium enim quasi est quidem magnam voluptate ipsam eos\ntempora quo necessitatibus\ndolor quam autem quasi\nreiciendis et nam sapiente accusantium"
  },
  {
    "postId": 1,
    "id": 2,
    "name": "quo vero reiciendis velit similique earum",
    "email": "Jayne_Kuhic@sydney.com",
    "body": "est natus enim nihil est dolore omnis voluptatem numquam\net omnis occaecati quod ullam at\nvoluptatem error expedita pariatur\nnihil sint nostrum voluptatem reiciendis et"
  },
  {
    "postId": 1,
    "id": 3,
    "name": "odio adipisci rerum aut animi",
    "email": "Nikita@garfield.biz",
    "body": "quia molestiae reprehenderit quasi aspernatur\naut expedita occaecati aliquam eveniet laudantium\nomnis quibusdam delectus saepe quia accusamus maiores nam est\ncum et ducimus et vero voluptates excepturi deleniti ratione"
  },
  {
    "postId": 1,
    "id": 4,
    "name": "alias odio sit",
    "email": "Lew@alysha.tv",
    "body": "non et atque\noccaecati deserunt quas accusantium unde odit nobis qui voluptatem\nquia voluptas consequuntur itaque dolor\net qui rerum deleniti ut occaecati"
  },
  {
    "postId": 1,
    "id": 5,
    "name": "vero eaque aliquid doloribus et culpa",
    "email": "Hayden@althea.biz",
    "body": "harum non quasi et ratione\ntempore iure ex voluptates in ratione\nharum architecto fugit inventore cupiditate\nvoluptates magni quo et"
  }
]

### Request 5 — Deliberate 404
curl -i https://jsonplaceholder.typicode.com/posts/999999

### RESPONSE

HTTP/2 404
date: Tue, 06 Oct 2026 09:51:26 GMT
content-type: application/json; charset=utf-8
content-length: 2
access-control-allow-credentials: true
cache-control: max-age=43200
etag: W/"2-vyGp6PvFo4RvsFtPoIWeCReyIC8"
expires: -1
pragma: no-cache
vary: Origin, Accept-Encoding
x-content-type-options: nosniff
x-powered-by: Express
x-ratelimit-limit: 3600000
x-ratelimit-remaining: 3599998
x-ratelimit-reset: 1791274735
nel: {"report_to":"cf-nel","success_fraction":0.0,"max_age":604800}
age: 7638
cf-cache-status: HIT
server: cloudflare
cf-ray: a463c47dfb077e08-SIN
alt-svc: h3=":443"; ma=86400

{}

Annotation
- Status: 404 — The requested resource was not found.
- Content-Type: application/json — The response body is JSON.
- Why this request failed: Post 999999 does not exist in the API.