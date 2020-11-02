#!/usr/bin/env bash

set -euo pipefail
my_dir="$(dirname "$(readlink -e "$0")")"

readonly lambda_jar="${my_dir}/server/target/awswebsocketdemo-lambda.jar"
[ -f "${lambda_jar}" ] || {
  echo "lambda jar (${lambda_jar}) not present, packaging..." >&2
  mvn package -DskipTests
}

find server/src/main | while read file; do
  if [ "${file}" -nt "${lambda_jar}" ]; then
    echo "'${file}' is newer than '${lambda_jar}', re-packaging..." >&2
    mvn package -DskipTests
  fi
done

readonly bucket_stack_name="${STACK_IDENTIFIER}-bucket"
readonly lambda_stack_name="${STACK_IDENTIFIER}-lambda"
readonly s3_bucket="${STACK_IDENTIFIER}-bucket"
readonly s3_key="awswebsocketdemo-lambda-$(md5sum < "${lambda_jar}" | cut -d ' ' -f 1).jar"

aws cloudformation deploy \
  --no-fail-on-empty-changeset \
  --stack-name "${s3_bucket}" \
  --parameter-overrides \
      ArtifactBucketName="${s3_bucket}" \
      StackIdentifier="${STACK_IDENTIFIER}" \
  --template-file cf-bucket.yml

aws s3api head-object --bucket "${s3_bucket}" --key "${s3_key}" &> /dev/null ||
  aws s3 cp "${lambda_jar}" "s3://${s3_bucket}/${s3_key}"

aws cloudformation deploy \
  --capabilities CAPABILITY_NAMED_IAM \
  --no-fail-on-empty-changeset \
  --stack-name "${lambda_stack_name}" \
  --parameter-overrides \
      ArtifactBucketName="${s3_bucket}" \
      StackIdentifier="${STACK_IDENTIFIER}" \
      ArtifactKey="${s3_key}" \
   --template-file cf-lambda.yml

eval "export $(aws cloudformation describe-stacks --stack-name "${lambda_stack_name}" --output text |
    grep -E "OUTPUTS" | awk '{print $3 "=" $4}' | tee /dev/stderr)"

cat <<EOF
HTTP endpoint: "${HttpEndpoint}"
Websocket endpoint: "${WebSocketEndpoint}"
EOF