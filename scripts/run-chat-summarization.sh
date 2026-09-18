#!/usr/bin/env bash
# Usage: scripts/run-chat-summarization.sh
set -euo pipefail
ARGFILE="$("$(dirname "$0")/tornado-args.sh")"
CMD=(java
    "@$ARGFILE"
    --add-modules jdk.incubator.vector
    -jar demos/chat-summarization/target/quarkus-app/quarkus-run.jar)
echo "${CMD[*]}"
"${CMD[@]}"
