set -eu
docker run --rm \
  -v$(pwd):/mnt/ \
  -v ~/.m2:/root/.m2 \
  -w "/mnt" \
  --env AWS_REGION=${AWS_REGION} \
  kgraalxcompile:1.0 "/bin/bash" "/mnt/build-graal.sh"
