#!/usr/bin/env bash
# Usage: scripts/run-java-coder-iterative.sh ["<prompt>"]
# Default prompt: "Write a matrix multiplication Java program"
PROMPT="${*:-Write a matrix multiplication Java program}"
ARGFILE="$("$(dirname "$0")/tornado-args.sh")"
CMD=(java
    "@$ARGFILE"
    --add-modules jdk.incubator.vector
    -jar demos/java-coder-iterative/target/quarkus-app/quarkus-run.jar
    "$PROMPT")
echo "${CMD[*]}"
"${CMD[@]}"
