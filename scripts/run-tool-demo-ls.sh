#!/usr/bin/env bash
# Usage: scripts/run-tool-demo-ls.sh ["<prompt>"]
# Default prompt: "Show me what is inside /tmp"
PROMPT="${*:-Show me what is inside /tmp}"
ARGFILE="$("$(dirname "$0")/tornado-args.sh")"
CMD=(java
    "@$ARGFILE"
    --add-modules jdk.incubator.vector
    --enable-preview
    -jar demos/tool-demo-ls/target/quarkus-app/quarkus-run.jar
    "$PROMPT")
echo "${CMD[*]}"
"${CMD[@]}"
