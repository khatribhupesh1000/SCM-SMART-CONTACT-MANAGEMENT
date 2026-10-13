#!/usr/bin/env bash
set -euo pipefail
BASE="${BASE_URL:-http://localhost:8090}"
CONTACT_REF="${CONTACT_REF:-}"
if [[ -z "$CONTACT_REF" ]]; then echo "Set CONTACT_REF to an existing SCM contact ID." >&2; exit 2; fi
KEY="a4-$(date +%s)-$$"
BODY="$(CONTACT_REF="$CONTACT_REF" python3 -c 'import json,os; print(json.dumps({"contactRef":os.environ["CONTACT_REF"],"recipientEmail":"friend@example.com","message":"SCM Assignment 4 test"}))')"
OUT="${1:-curl-transcript-actual.txt}"
{
 echo '### 1. Create (expect 201 + Location)'
 curl -i -sS -X POST "$BASE/v1/contact-shares" -H 'Content-Type: application/json' -H 'Accept: application/json' -H "Idempotency-Key: $KEY" -d "$BODY"
 echo; echo '### 2. Repeat same create/key (original resource)'
 curl -i -sS -X POST "$BASE/v1/contact-shares" -H 'Content-Type: application/json' -H 'Accept: application/json' -H "Idempotency-Key: $KEY" -d "$BODY"
 echo; echo '### 3. Malformed JSON (expect 400)'
 curl -i -sS -X POST "$BASE/v1/contact-shares" -H 'Content-Type: application/json' -H 'Accept: application/json' -H "Idempotency-Key: bad-$KEY" -d '{'
 echo; echo '### 4. Missing resource (expect 404)'
 curl -i -sS "$BASE/v1/contact-shares/no-such-share" -H 'Accept: application/json'
 echo; echo '### 5. Cancellation state conflict (cancel same request twice)'
 ID="$(curl -sS -X POST "$BASE/v1/contact-shares" -H 'Content-Type: application/json' -H 'Accept: application/json' -H "Idempotency-Key: cancel-$KEY" -d "$BODY" | python3 -c 'import json,sys; print(json.load(sys.stdin)["share_id"])')"
 curl -i -sS -X POST "$BASE/v1/contact-shares/$ID/cancellation" -H 'Accept: application/json'
 curl -i -sS -X POST "$BASE/v1/contact-shares/$ID/cancellation" -H 'Accept: application/json'
} 2>&1 | tee "$OUT"
echo "Actual transcript saved to $OUT"
