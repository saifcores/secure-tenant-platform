#!/usr/bin/env bash
set -euo pipefail

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8081}"
API_URL="${API_URL:-http://localhost:8080}"
GLOBEX_ORDER="${GLOBEX_ORDER:-dddddddd-dddd-dddd-dddd-ddddddddddd1}"

token() {
  local user="$1"
  curl -s -X POST "${KEYCLOAK_URL}/realms/securetenant/protocol/openid-connect/token" \
    -d grant_type=password \
    -d client_id=securetenant-public \
    -d username="${user}" \
    -d password=password | jq -r .access_token
}

echo "== Step 3/4: Alice lists ACME orders"
ALICE_TOKEN="$(token alice)"
curl -s -H "Authorization: Bearer ${ALICE_TOKEN}" "${API_URL}/api/orders" | jq .

echo "== Step 5: Alice tries a GLOBEX order (expect 404)"
curl -s -o /tmp/globex-order.json -w "HTTP %{http_code}\n" \
  -H "Authorization: Bearer ${ALICE_TOKEN}" \
  "${API_URL}/api/orders/${GLOBEX_ORDER}"
jq . /tmp/globex-order.json || true

echo "== Step 6: Alice forges X-Tenant-ID: globex (still ACME)"
curl -s -H "Authorization: Bearer ${ALICE_TOKEN}" \
  -H "X-Tenant-ID: globex" \
  -H "X-TenantId: globex" \
  "${API_URL}/api/orders?tenantId=globex" | jq '[.[].tenantId] | unique'

echo "== Step 7: Audit trail"
curl -s -H "Authorization: Bearer ${ALICE_TOKEN}" "${API_URL}/api/audit" | jq '.[0:5]'

echo "== Bob cannot delete a customer (expect 403)"
BOB_TOKEN="$(token bob)"
curl -s -o /tmp/bob-delete.json -w "HTTP %{http_code}\n" \
  -X DELETE \
  -H "Authorization: Bearer ${BOB_TOKEN}" \
  "${API_URL}/api/customers/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1"
jq . /tmp/bob-delete.json || true

echo "== Alice confirms then pays ACME order (Idempotency-Key)"
curl -s -X PUT \
  -H "Authorization: Bearer ${ALICE_TOKEN}" \
  "${API_URL}/api/orders/cccccccc-cccc-cccc-cccc-ccccccccccc1/confirm" | jq '{id,status}'
curl -s -H "Authorization: Bearer ${ALICE_TOKEN}" \
  -H "Idempotency-Key: demo-alice-1" \
  -H "Content-Type: application/json" \
  -d '{"orderId":"cccccccc-cccc-cccc-cccc-ccccccccccc1"}' \
  "${API_URL}/api/payments" | jq '{id,status,amount,tenantId}'

echo "== Alice wallets + reconciliation + users"
curl -s -H "Authorization: Bearer ${ALICE_TOKEN}" "${API_URL}/api/wallets" | jq .
curl -s -H "Authorization: Bearer ${ALICE_TOKEN}" "${API_URL}/api/reconciliation" | jq '{balanced,wallets}'
curl -s -H "Authorization: Bearer ${ALICE_TOKEN}" "${API_URL}/api/users" | jq '[.[].username]'
