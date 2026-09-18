# Quarkus + LangChain4j + GPULlama3 Demos

A collection of Quarkus applications that drive [GPULlama3.java](https://github.com/beehive-lab/GPULlama3.java) via the
[quarkus-langchain4j](https://github.com/quarkiverse/quarkus-langchain4j) `gpu-llama3` provider. Each demo lives under
`demos/` as its own Maven module and can be built once at the repo root and run independently.

Inspired by: <https://docs.quarkiverse.io/quarkus-langchain4j/dev/quickstart-summarization.html>

## Demos at a glance

| Module | What it shows | Default prompt |
|---|---|---|
| [`demos/chat-summarization`](demos/chat-summarization) | Blocking chat-model summarization service | `SampleTextToSummarize.txt` |
| [`demos/streaming-summarization`](demos/streaming-summarization) | Token-streaming summarization service | `SampleTextToSummarize.txt` |
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
sdk install tornadovm 5.2.0-jdk25-metal   # or 5.2.0-jdk25-opencl / -ptx for your backend
sdk use     tornadovm 5.2.0-jdk25-metal
echo "$TORNADOVM_HOME"   # must be set; SDKMAN sets it automatically
```

TornadoVM **5.2.0** is required, not 6.x: the published `gpu-llama3:1.0.0-jdk25` is compiled
against `tornado-api` 5.0.0, and TornadoVM 6.0.0 made `TornadoFunctions.TaskN` serializable, so
on 6.x every kernel fails with `Kernel entry ... has no writeReplace()`. To run on TornadoVM
6.0.0, rebuild the GPULlama3 `v1.0.0` tag against it and point `<gpu-llama3.version>` at that
build — see the comment in [`pom.xml`](pom.xml).

> **`sdk use` vs `sdk default`:** `$TORNADOVM_HOME/tornado-argfile` hardcodes paths under
> `.../tornadovm/current/`, i.e. the SDKMAN *default* install — so `sdk use` alone can leave the
> demos running on a different TornadoVM than `$TORNADOVM_HOME` names. The run scripts avoid this
> by expanding `tornado-argfile.template` via [`scripts/tornado-args.sh`](scripts/tornado-args.sh);
> pass the same argfile to the build (see below) or use `sdk default`.

### Versions

Everything else comes from Maven Central — no local builds needed:

| Component | Version |
|---|---|
| Quarkus | 3.33.3.1 |
| quarkus-langchain4j (`gpu-llama3` provider) | 1.14.0.CR3 |
| `io.github.beehive-lab:gpu-llama3` | 1.0.0-jdk25 |
| TornadoVM | 5.2.0-jdk25 |

All four are pinned in the root [`pom.xml`](pom.xml). They are interdependent: a locally installed
jar that reuses one of these coordinates (e.g. a `999-SNAPSHOT` quarkus-langchain4j, or a
`gpu-llama3` built from GPULlama3 `main`) shadows the published one and surfaces as a runtime
`NoSuchMethodError` / `ClassNotFoundException`, not a build failure.

## 2. Build all demos

From the repo root:

```bash
./mvnw clean install -Dtornado.argfile="$(scripts/tornado-args.sh)"
```

This produces a runnable `target/quarkus-app/quarkus-run.jar` inside each `demos/<demo>/` directory.

> **First-run note:** each demo downloads its GGUF model to `~/.langchain4j/models/` on first launch.

## 3. Run a demo

Two equivalent options for every demo: the helper script under `scripts/`, or `java @tornado-argfile ... -jar ...` directly.

### chat-summarization (blocking)

```bash
scripts/run-chat-summarization.sh
```

With batched prefill-decode:

```bash
java "@$(scripts/tornado-args.sh)" \
    --add-modules jdk.incubator.vector \
    -Dllama.batchedPrefill=true \
    -Dllama.prefillBatchSize=32 \
    -jar demos/chat-summarization/target/quarkus-app/quarkus-run.jar
```

### streaming-summarization (token-streamed)

```bash
scripts/run-streaming-summarization.sh
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
- `quarkus.langchain4j.gpu-llama3.chat-model.device-memory` — TornadoVM heap, tune to your GPU.
  Since quarkus-langchain4j 1.14 the extension sets this itself, so the old `-Dtornado.device.memory`
  JVM flag is ignored (its 4GB default OOMs on F16 models).
- `-Dllama.batchedPrefill=true -Dllama.prefillBatchSize=<N>` — enable batched prefill-decode

For tool-calling fidelity, a 3B model is noticeably more reliable than 1B; swap the `model-name`/`quantization`
properties in the relevant `application.properties`.
