#!/usr/bin/env bash
set -euo pipefail

curl -X POST \
  --header 'Content-Type: application/json' \
  "${HTTP_ENDPOINT}/disconnect_everyone"
