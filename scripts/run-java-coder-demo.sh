#!/usr/bin/env bash
# Usage: scripts/run-java-coder-demo.sh ["<prompt>"]
# Default prompt: "Write a Java class to print HelloWorld"
PROMPT="${*:-Write a Java class to print HelloWorld}"
ARGFILE="$("$(dirname "$0")/tornado-args.sh")"
CMD=(java
    "@$ARGFILE"
    --add-modules jdk.incubator.vector
    -jar demos/java-coder-demo/target/quarkus-app/quarkus-run.jar
    "$PROMPT")
echo "${CMD[*]}"
"${CMD[@]}"
