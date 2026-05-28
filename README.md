# Quarkus + LangChain4j + GPULlama3 Demos

A collection of Quarkus applications that drive [GPULlama3.java](https://github.com/beehive-lab/GPULlama3.java) via the
[quarkus-langchain4j](https://github.com/quarkiverse/quarkus-langchain4j) `gpu-llama3` provider. Each demo lives under
`demos/` as its own Maven module and can be built once at the repo root and run independently.

Inspired by: <https://docs.quarkiverse.io/quarkus-langchain4j/dev/quickstart-summarization.html>

## Demos at a glance

| Module | What it shows | Default prompt |
|---|---|---|
| [`demos/chat-demo`](demos/chat-demo) | Blocking chat-model summarization service | `SampleTextToSummarize.txt` |
| [`demos/streaming-demo`](demos/streaming-demo) | Token-streaming summarization service | `SampleTextToSummarize.txt` |
| [`demos/tool-demo-ls`](demos/tool-demo-ls) | Tool calling — LLM invokes a `listDirectory` tool | `Show me what is inside /tmp` |
| [`demos/java-coder-demo`](demos/java-coder-demo) | Host-controlled code generation + execution | `Write a Hello World Java program` |
| [`demos/java-coder-iterative`](demos/java-coder-iterative) | Generate → compile → ask LLM to fix on error (up to 3 attempts) | `Write a matrix multiplication Java program` |

## 1. Prerequisites

### Java 25

```bash
sdk install java 25.0.2-open
sdk use     java 25.0.2-open
java -version   # openjdk 25 ...
```

### TornadoVM

```bash
sdk install tornadovm 4.0.1-jdk25-ptx   # or 4.0.0-jdk25-ptx
sdk use     tornadovm 4.0.1-jdk25-ptx
echo "$TORNADOVM_HOME"   # must be set; SDKMAN sets it automatically
```

### Build quarkus-langchain4j from source (JDK 25 support)

> As of 2026-02-04, the published `quarkus-langchain4j` artifacts are not compiled with JDK 25.
> Build the `gpu-llama3` provider locally so it is installed into your local Maven repo.

```bash
git clone https://github.com/quarkiverse/quarkus-langchain4j.git
cd quarkus-langchain4j/model-providers/gpu-llama3
mvn clean install -DskipTests -DTornado
```

This installs `quarkus-langchain4j-gpu-llama3:1.10.0` (matching `<quarkus-langchain4j.version>` in [`pom.xml`](pom.xml)).

## 2. Build all demos

From the repo root:

```bash
./mvnw clean install
```

This produces a runnable `target/quarkus-app/quarkus-run.jar` inside each `demos/<demo>/` directory.

> **First-run note:** each demo downloads its GGUF model to `~/.langchain4j/models/` on first launch.

## 3. Run a demo

Two equivalent options for every demo: the helper script under `scripts/`, or `java @tornado-argfile ... -jar ...` directly.

### chat-demo (summarization, blocking)

```bash
scripts/run-chat.sh
```

With batched prefill-decode:

```bash
java @$TORNADOVM_HOME/tornado-argfile \
    --add-modules jdk.incubator.vector \
    -Dtornado.device.memory=8GB \
    -Dllama.batchedPrefill=true \
    -Dllama.prefillBatchSize=32 \
    -jar demos/chat-demo/target/quarkus-app/quarkus-run.jar
```

### streaming-demo (summarization, token-streamed)

```bash
scripts/run-streaming.sh
```

### tool-demo-ls (tool calling)

```bash
scripts/run-tool-demo-ls.sh                          # default: "Show me what is inside /tmp"
scripts/run-tool-demo-ls.sh "Show me what is inside $HOME/Desktop"
```

The prompt is forwarded to the demo. `log-requests` / `log-responses` are enabled in this demo's
`application.properties`, so the full tool-call round-trip is visible in the Quarkus log.

### java-coder-demo (generate + run)

```bash
scripts/run-java-coder-demo.sh                       # default: "Write a Hello World Java program"
scripts/run-java-coder-demo.sh "Write a matmul in Java"
```

### java-coder-iterative (generate → compile → fix loop)

```bash
scripts/run-java-coder-iterative.sh                  # default: matrix multiplication
scripts/run-java-coder-iterative.sh "Write a quicksort in Java"
```

## 4. Dev mode (no need to repackage)

Each module can also be run via `quarkus:dev`, which is handy when iterating on a single demo:

```bash
cd demos/tool-demo-ls
mvn quarkus:dev -Dquarkus.args="Show me what is inside /tmp"
```

See [`devoxx-athens-demo.md`](devoxx-athens-demo.md) for a worked sequence of dev-mode commands used at the Devoxx Athens demo.

## 5. Tuning

Each demo has its own `src/main/resources/application.properties`. Common knobs:

- `quarkus.langchain4j.gpu-llama3.chat-model.model-name` — Hugging Face GGUF repo (e.g. `unsloth/Llama-3.2-1B-Instruct-GGUF`)
- `quarkus.langchain4j.gpu-llama3.chat-model.quantization` — `Q8_0`, `F16`, ...
- `quarkus.langchain4j.gpu-llama3.chat-model.temperature`, `.top-p`, `.max-tokens`
- `-Dtornado.device.memory=<N>GB` on the JVM — tune to your GPU
- `-Dllama.batchedPrefill=true -Dllama.prefillBatchSize=<N>` — enable batched prefill-decode

For tool-calling fidelity, a 3B model is noticeably more reliable than 1B; swap the `model-name`/`quantization`
properties in the relevant `application.properties`.
