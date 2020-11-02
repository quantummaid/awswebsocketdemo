#!/usr/bin/env bash

set -euo pipefail
my_dir="$(dirname "$(readlink -e "$0")")"

java -jar "${my_dir}/client/target/democlient.jar" "$@"