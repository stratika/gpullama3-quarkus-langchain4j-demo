package demo;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class JavaCoderTools {

    private static final Logger LOG = Logger.getLogger(JavaCoderTools.class);

    static final Path WORKSPACE = Path.of(System.getProperty("java.io.tmpdir"), "gpu-llama3-java-coder-iterative");
    private static final Path JAVA_BIN = Path.of(System.getProperty("java.home"), "bin");

    // Stores the raw result of the last buildAndRun call so the host can
    // check compilation status without parsing the runner agent's synthesis.
    private String lastBuildResult;

    public String getLastBuildResult() {
        return lastBuildResult;
    }

    public void writeFile(String filename, String code) {
        try {
            Files.createDirectories(WORKSPACE);
            Files.writeString(WORKSPACE.resolve(filename), code);
            LOG.infof("[JavaCoder] wrote %s (%d chars)", filename, code.length());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write " + filename, e);
        }
    }

    @Tool("Builds and runs a Java program and returns the compilation result and program output")
    public String buildAndRun(
            @P("source filename, e.g. HelloWorld.java") String filename,
            @P("main class name, e.g. HelloWorld") String className) {

        String compileResult = compile(filename);
        String result;
        if (!compileResult.equals("OK")) {
            LOG.warnf("[JavaCoder] build failed for %s", filename);
            result = "Compilation FAILED:\n" + compileResult;
        } else {
            result = "Compilation: OK\nOutput:\n" + run(className);
        }
        this.lastBuildResult = result;
        return result;
    }

    private String compile(String filename) {
        try {
            Process process = new ProcessBuilder(JAVA_BIN.resolve("javac").toString(), "-d", ".", filename)
                    .directory(WORKSPACE.toFile())
                    .redirectErrorStream(true)
                    .start();
            String output = new String(process.getInputStream().readAllBytes());
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return "Compilation timed out.";
            }
            if (process.exitValue() == 0) {
                LOG.infof("[JavaCoder] compiled %s — OK", filename);
                return "OK";
            }
            LOG.warnf("[JavaCoder] compiled %s — FAILED", filename);
            return output;
        } catch (IOException | InterruptedException e) {
            return "Compiler error: " + e.getMessage();
        }
    }

    private String run(String className) {
        try {
            Process process = new ProcessBuilder(JAVA_BIN.resolve("java").toString(), "-cp", ".", className)
                    .directory(WORKSPACE.toFile())
                    .redirectErrorStream(true)
                    .start();
            String output = new String(process.getInputStream().readAllBytes());
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return "(execution timed out)";
            }
            LOG.infof("[JavaCoder] ran %s — exit %d", className, process.exitValue());
            return output.isBlank() ? "(no output)" : output.stripTrailing();
        } catch (IOException | InterruptedException e) {
            return "Run error: " + e.getMessage();
        }
    }
}
