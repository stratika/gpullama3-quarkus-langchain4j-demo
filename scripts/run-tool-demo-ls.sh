#!/usr/bin/env bash
# Usage: scripts/run-tool-demo-ls.sh ["<prompt>"]
# Default prompt: "Show me what is inside /tmp"
PROMPT="${*:-Show me what is inside /tmp}"
CMD=(java
    "@$TORNADOVM_HOME/tornado-argfile"
    --add-modules jdk.incubator.vector
    --enable-preview
    -Dtornado.device.memory=8GB
    -jar demos/tool-demo-ls/target/quarkus-app/quarkus-run.jar
    "$PROMPT")
echo "${CMD[*]}"
"${CMD[@]}"
