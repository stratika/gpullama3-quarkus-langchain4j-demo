Summarization Service Quarkus application

inspired by : https://docs.quarkiverse.io/quarkus-langchain4j/dev/quickstart-summarization.html#

### Environment
1) Java 25
```bash
sdk use java 25.0.2-open
```
2) TornadoVM
``` bash
sdk use tornadovm 4.0.1-jdk25-ptx
```

3) Manual Clone and Build Quarkus-langchain4j to enable jdk25 support:
- `Note: As of 2/4/2026 Quarkus-langchain4j artifacts are not compiled with jdk25; hence we need to build from source.` 
```bash
git clone https://github.com/quarkiverse/quarkus-langchain4j.git
cd ~/quarkus-langchain4j/model-provivers/gpu-llama3
mvn clean install -DskipTests -DTornado
```

### Build
Build all demos:
```bash
cd <your-path>/gpullama3-quarkus-langchain4j-demo
mvn clean install
```

### Run:
Run chat-demo:
``` bash
java @$TORNADOVM_HOME/tornado-argfile \
    --add-modules jdk.incubator.vector \
    -Dtornado.device.memory=15GB \
    -jar demos/chat-demo/target/quarkus-app/quarkus-run.jar
```

- with batched prefill-decode
```bash
java @$TORNADOVM_HOME/tornado-argfile \
    --add-modules jdk.incubator.vector \
    -Dtornado.device.memory=15GB \
    -Dllama.batchedPrefill=true \
    -Dllama.prefillBatchSize=32 \
    -jar demos/chat-demo/target/quarkus-app/quarkus-run.jar
```

Run streaming-demo:
``` bash
java @$TORNADOVM_HOME/tornado-argfile \
    --add-modules jdk.incubator.vector \
    -Dtornado.device.memory=15GB \
    -jar demos/streaming-demo/target/quarkus-app/quarkus-run.jar
```
