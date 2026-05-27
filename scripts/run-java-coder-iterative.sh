#!/usr/bin/env bash
# Usage: scripts/run-java-coder-iterative.sh ["<prompt>"]
# Default prompt: "Write a matrix multiplication Java program"
PROMPT="${*:-Write a matrix multiplication Java program}"
CMD=(java
    "@$TORNADOVM_HOME/tornado-argfile"
    --add-modules jdk.incubator.vector
    -Dtornado.device.memory=8GB
    -jar demos/java-coder-iterative/target/quarkus-app/quarkus-run.jar
    "$PROMPT")
echo "${CMD[*]}"
"${CMD[@]}"
