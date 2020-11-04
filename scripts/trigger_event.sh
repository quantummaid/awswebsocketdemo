#!/usr/bin/env bash
set -euo pipefail

curl -X POST \
  --data "
  {
    \"clientId\": \"${CLIENT_ID}\",
    \"event\": {
      \"value1\": \"qwer\",
      \"value2\": \"asdf\",
      \"value3\": \"yxcv\"
    }
  }
  " \
  --header 'Content-Type: application/json' \
  "${HTTP_ENDPOINT}/trigger_event"
