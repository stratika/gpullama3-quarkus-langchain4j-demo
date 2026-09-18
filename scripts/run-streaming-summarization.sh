#!/usr/bin/env bash
# Usage: scripts/run-streaming-summarization.sh
set -euo pipefail
ARGFILE="$("$(dirname "$0")/tornado-args.sh")"
CMD=(java
    "@$ARGFILE"
    --add-modules jdk.incubator.vector
    -jar demos/streaming-summarization/target/quarkus-app/quarkus-run.jar)
echo "${CMD[*]}"
"${CMD[@]}"
