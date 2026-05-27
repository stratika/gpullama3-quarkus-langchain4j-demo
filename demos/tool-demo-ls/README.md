# tool-demo-ls

Demonstrates tool calling with GPULlama3.java via Quarkus LangChain4j.
The AI service is given a `listDirectory` tool and must call it to answer questions about directory contents.

## What it does

1. The LLM receives the user prompt and a `listDirectory` tool definition.
2. It emits a `<tool_call>` response naming the tool and the path argument.
3. Quarkus LangChain4j executes `DirectoryTools.listDirectory` (Java NIO, no shell).
4. The result is fed back to the LLM, which produces a plain-text answer.

---

## Prerequisites

### 1. Java 25

```bash
sdk install java 25.0.2-open
sdk use java 25.0.2-open
```

Verify:
```bash
java -version   # openjdk 25 ...
```

### 2. TornadoVM

```bash
sdk install tornadovm 4.0.0-jdk25-ptx
sdk use tornadovm 4.0.0-jdk25-ptx
```

`$TORNADOVM_HOME` must be set (SDKMAN sets it automatically).

### 3. GPULlama3.java

```bash
git clone https://github.com/beehive-lab/GPULlama3.java.git
cd GPULlama3.java
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.2-open mvn install -DskipTests -q
```

This installs `gpu-llama3-0.4.0-jdk25.jar` into the local Maven repository.

### 4. quarkus-langchain4j gpu-llama3 provider

The GPU Llama3 provider is not yet published; build from source:

```bash
git clone https://github.com/quarkiverse/quarkus-langchain4j.git
cd quarkus-langchain4j
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.2-open \
  mvn install -pl model-providers/gpu-llama3/runtime,model-providers/gpu-llama3/deployment \
  -am -DskipTests -q
```

This installs `quarkus-langchain4j-gpu-llama3:999-SNAPSHOT` into the local Maven repository.

---

## Build

From the demo directory:

```bash
cd Quarkus-Langchain4j-GPULlama3-Demos/demos/tool-demo-ls
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.2-open mvn package -DskipTests -q
```

Or build all demos from the parent:

```bash
cd Quarkus-Langchain4j-GPULlama3-Demos
JAVA_HOME=$HOME/.sdkman/candidates/java/25.0.2-open mvn package -DskipTests -q
```

---

## Configuration

Edit `src/main/resources/application.properties` to switch the model or tune sampling:

```properties
# Llama 3.2 1B (default)
quarkus.langchain4j.gpu-llama3.chat-model.model-name=unsloth/Llama-3.2-1B-Instruct-GGUF
quarkus.langchain4j.gpu-llama3.chat-model.quantization=Q8_0
quarkus.langchain4j.gpu-llama3.chat-model.temperature=0.3
quarkus.langchain4j.gpu-llama3.chat-model.top-p=0.95
quarkus.langchain4j.gpu-llama3.chat-model.max-tokens=2048
```

For better tool-calling synthesis, prefer a 3B or 8B model.

The model is downloaded automatically on first run to `~/.langchain4j/models/`.

---

## Run

```bash
java @$TORNADOVM_HOME/tornado-argfile \
    --add-modules jdk.incubator.vector \
    --enable-preview \
    -Dtornado.device.memory=15GB \
    -jar target/quarkus-app/quarkus-run.jar \
    "Show me what is inside /tmp"
```

The prompt is passed as command-line arguments after the jar. Without arguments it defaults to `"Show me what is inside /tmp"`.

### With batched prefill-decode

```bash
java @$TORNADOVM_HOME/tornado-argfile \
    --add-modules jdk.incubator.vector \
    --enable-preview \
    -Dtornado.device.memory=15GB \
    -Dllama.batchedPrefill=true \
    -Dllama.prefillBatchSize=32 \
    -jar target/quarkus-app/quarkus-run.jar \
    "Show me what is inside /home/orion/Desktop"
```

---

## Expected output

```
Tool Calling Demo — listDirectory
==================================
Prompt: Show me what is inside /tmp

Answer:
-------
The contents of /tmp are:
file: foo.txt, 1024 bytes
dir:  systemd-private-...
...
```

`log-requests` and `log-responses` are enabled in `application.properties`, so the full tool-call round-trip (tool schema injection, `<tool_call>` response, tool result, final answer) is visible in the Quarkus log output.
