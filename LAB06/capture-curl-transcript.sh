#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8090}"
CONTACT_REF="${CONTACT_REF:-}"
TOKEN="${SCM_DEMO_BEARER_TOKEN:-scm-assignment5-demo-token}"
OUT="${1:-transcript-actual.txt}"
if [[ -z "$CONTACT_REF" ]]; then
  echo "Set CONTACT_REF to an existing SCM contact ID: CONTACT_REF=cnt-existing-id ./capture-curl-transcript.sh" >&2
  exit 2
fi
KEY="scm-a6-$(date +%s)-$$"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
BODY="{\"contactRef\":\"$CONTACT_REF\",\"recipientEmail\":\"friend@example.com\",\"message\":\"SCM Assignment 6 validation\"}"
AUTH=(-H "Authorization: Bearer $TOKEN" -H 'Accept: application/json')
{
  echo '### D1. Successful create (expect 201 + Location + JSON)'
  curl -i -sS -X POST "$BASE_URL/v1/contact-shares" "${AUTH[@]}" -H 'Content-Type: application/json' -H "Idempotency-Key: $KEY" -H 'X-Client-ID: a6-success' -d "$BODY"
  echo; echo '### D4. Same create/key/payload (idempotent replay)'
  curl -i -sS -X POST "$BASE_URL/v1/contact-shares" "${AUTH[@]}" -H 'Content-Type: application/json' -H "Idempotency-Key: $KEY" -H 'X-Client-ID: a6-replay' -d "$BODY"
  curl -sS -X POST "$BASE_URL/v1/contact-shares" "${AUTH[@]}" -H 'Content-Type: application/json' -H "Idempotency-Key: $KEY" -H 'X-Client-ID: a6-id' -d "$BODY" > "$TMP/create.json"
  SHARE_ID="$(sed -n 's/.*"share_id"[[:space:]]*:[[:space:]]*"\\([^"]*\\)".*/\\1/p' "$TMP/create.json" | head -n 1)"
  echo; echo '### B3/C2. Multiple invalid fields in one request (expect 422 + errors[])'
  curl -i -sS -X POST "$BASE_URL/v1/contact-shares" "${AUTH[@]}" -H 'Content-Type: application/json' -H "Idempotency-Key: bad-fields-$KEY" -H 'X-Client-ID: a6-invalid' -d '{"contactRef":"","recipientEmail":"not-an-email"}'
  echo; echo '### Malformed JSON (expect 400)'
  curl -i -sS -X POST "$BASE_URL/v1/contact-shares" "${AUTH[@]}" -H 'Content-Type: application/json' -H "Idempotency-Key: malformed-$KEY" -H 'X-Client-ID: a6-malformed' -d '{'
  echo; echo '### Unknown resource (expect 404)'
  curl -i -sS "$BASE_URL/v1/contact-shares/no-such-share" "${AUTH[@]}" -H 'X-Client-ID: a6-missing'
  echo; echo '### Illegal transition (cancel the same share twice; second should be 409)'
  curl -i -sS -X POST "$BASE_URL/v1/contact-shares/$SHARE_ID/cancellation" "${AUTH[@]}" -H 'X-Client-ID: a6-cancel-1'
  curl -i -sS -X POST "$BASE_URL/v1/contact-shares/$SHARE_ID/cancellation" "${AUTH[@]}" -H 'X-Client-ID: a6-cancel-2'
  echo; echo '### Unsupported XML Accept (expect 406 because XML is not implemented)'
  curl -i -sS "$BASE_URL/v1/contact-shares" -H "Authorization: Bearer $TOKEN" -H 'Accept: application/xml' -H 'X-Client-ID: a6-xml'
  echo; echo '### Missing authentication (expect 401 Problem Details)'
  curl -i -sS "$BASE_URL/v1/contact-shares" -H 'Accept: application/json' -H 'X-Client-ID: a6-no-auth'
  echo; echo '### Assignment 5 conditional GET regression (expect 304)'
  curl -sS -D "$TMP/read.headers" -o /dev/null "$BASE_URL/v1/contact-shares/$SHARE_ID" "${AUTH[@]}" -H 'X-Client-ID: a6-etag'
  ETAG="$(awk 'BEGIN{IGNORECASE=1} /^ETag:/{gsub("\\r",""); sub(/^[^:]*:[[:space:]]*/,""); print; exit}' "$TMP/read.headers")"
  curl -i -sS "$BASE_URL/v1/contact-shares/$SHARE_ID" "${AUTH[@]}" -H "If-None-Match: $ETAG" -H 'X-Client-ID: a6-etag2'
  echo; echo '### Assignment 5 stale update regression (expect 412)'
  curl -i -sS -X PATCH "$BASE_URL/v1/contact-shares/$SHARE_ID" "${AUTH[@]}" -H 'Content-Type: application/json' -H 'If-Match: "stale-etag"' -H 'X-Client-ID: a6-patch' -d '{"message":"must not overwrite"}'
  echo; echo '### Rate limit demonstration (start service with RATE_LIMIT_PER_MINUTE=2; expect third request 429 + Retry-After)'
  for i in 1 2 3; do
    curl -i -sS "$BASE_URL/v1/contact-shares" "${AUTH[@]}" -H 'X-Client-ID: a6-rate-limit-demo'
  done
} 2>&1 | tee "$OUT"
echo "Captured actual HTTP output to $OUT"
