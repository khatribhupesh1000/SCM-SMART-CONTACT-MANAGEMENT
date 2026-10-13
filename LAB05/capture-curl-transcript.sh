#!/usr/bin/env bash
set -euo pipefail
BASE="${BASE_URL:-http://localhost:8090}"
CONTACT_REF="${CONTACT_REF:-}"
TOKEN="${SCM_DEMO_BEARER_TOKEN:-scm-assignment5-demo-token}"
CLIENT="${X_CLIENT_ID:-team12-demo}"
if [[ -z "$CONTACT_REF" ]]; then echo "Set CONTACT_REF to an existing SCM contact ID." >&2; exit 2; fi
OUT="${1:-curl-transcript-actual.txt}"
KEY="a5-$(date +%s)-$$"
BODY="$(CONTACT_REF="$CONTACT_REF" python3 -c 'import json,os; print(json.dumps({"contactRef":os.environ["CONTACT_REF"],"recipientEmail":"friend@example.com","message":"SCM Assignment 5 test"}))')"
AUTH=(-H "Authorization: Bearer $TOKEN" -H 'Accept: application/json' -H "X-Client-ID: $CLIENT")
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
{
 echo '### 1. Create (expect 201 + Location)'
 curl -v -i -X POST "$BASE/v1/contact-shares" "${AUTH[@]}" -H 'Content-Type: application/json' -H "Idempotency-Key: $KEY" -d "$BODY" 2>&1
 echo; echo '### 2. Repeat same create/key (expect original resource)'
 curl -v -i -X POST "$BASE/v1/contact-shares" "${AUTH[@]}" -H 'Content-Type: application/json' -H "Idempotency-Key: $KEY" -d "$BODY" 2>&1
 curl -sS -X POST "$BASE/v1/contact-shares" "${AUTH[@]}" -H 'Content-Type: application/json' -H "Idempotency-Key: $KEY" -d "$BODY" > "$TMP/create.json"
 ID="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["share_id"])' "$TMP/create.json")"
 echo; echo '### 3. Read one resource and obtain ETag'
 curl -v -i "$BASE/v1/contact-shares/$ID" "${AUTH[@]}" 2>&1
 curl -sS -D "$TMP/headers" -o /dev/null "$BASE/v1/contact-shares/$ID" "${AUTH[@]}"
 ETAG="$(awk 'BEGIN{IGNORECASE=1} /^ETag:/{gsub("\\r",""); sub(/^[^:]*:[[:space:]]*/,""); print; exit}' "$TMP/headers")"
 echo; echo '### 4. Conditional GET (expect 304)'
 curl -v -i "$BASE/v1/contact-shares/$ID" "${AUTH[@]}" -H "If-None-Match: $ETAG" 2>&1
 echo; echo '### 5. Stale conditional PATCH (expect 412)'
 curl -v -i -X PATCH "$BASE/v1/contact-shares/$ID" "${AUTH[@]}" -H 'Content-Type: application/json' -H 'If-Match: "stale-validator"' -d '{"message":"must not overwrite"}' 2>&1
 echo; echo '### 6. Malformed JSON (expect 400)'
 curl -v -i -X POST "$BASE/v1/contact-shares" "${AUTH[@]}" -H 'Content-Type: application/json' -H "Idempotency-Key: bad-$KEY" -d '{' 2>&1
 echo; echo '### 7. Missing resource (expect 404)'
 curl -v -i "$BASE/v1/contact-shares/no-such-share" "${AUTH[@]}" 2>&1
 echo; echo '### 8. Missing Authorization (expect 401)'
 curl -v -i "$BASE/v1/contact-shares" -H 'Accept: application/json' 2>&1
 echo; echo '### 9. OPTIONS and Allow (expect 204)'
 curl -v -i -X OPTIONS "$BASE/v1/contact-shares/$ID" -H 'Origin: https://example.org' 2>&1
} 2>&1 | tee "$OUT"
echo "Actual transcript saved to $OUT"
