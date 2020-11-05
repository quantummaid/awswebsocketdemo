#!/usr/bin/env bash
set -euo pipefail

curl -X POST \
  --data "
  {
    \"clientGroup\": \"${GROUP_ID}\",
    \"event\": {
      \"message\": \"Hello to the world of C0d3rs\",
      \"motivation\": \"Code faster!\"
    }
  }
  " \
  --header 'Content-Type: application/json' \
  "${HTTP_ENDPOINT}/broadcast_event"
