#!/bin/bash

EDA_WEBHOOK_URL="https://aap.domain.tld/eda-event-streams/api/eda/v1/external_event_stream/1234-abc-5678-def-9012345/post/"
HMAC_SECRET="secret"

PAYLOAD='{
  "task_result_callback_url": "https://app.terraform.io/api/v2/task-results/taskres-12345/callback",
  "access_token": "dummy-token",
  "workspace_name": "test-workspace"
}'

SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$HMAC_SECRET" | awk '{print $2}')

echo "Calculated Signature: $SIGNATURE"

curl -X POST "$EDA_WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -H "x-tfc-task-signature: $SIGNATURE" \
  -d "$PAYLOAD"
