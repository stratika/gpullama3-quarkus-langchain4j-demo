#!/usr/bin/env bash
# Usage: scripts/run-java-coder-demo.sh ["<prompt>"]
# Default prompt: "Write a Hello World Java program"
PROMPT="${*:-Write a Hello World Java program}"
CMD=(java
    "@$TORNADOVM_HOME/tornado-argfile"
    --add-modules jdk.incubator.vector
    -Dtornado.device.memory=8GB
    -jar demos/java-coder-demo/target/quarkus-app/quarkus-run.jar
    "$PROMPT")
echo "${CMD[*]}"
"${CMD[@]}"
