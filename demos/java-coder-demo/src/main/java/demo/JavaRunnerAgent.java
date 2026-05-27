package demo;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Builds and reports the output of a prepared Java source file.
 * Uses a single buildAndRun tool — the right granularity for a 1B model.
 * Mirrors WeatherForecastAgent: system message describes the role, what the
 * tool returns, and the expected response format.
 */
@ApplicationScoped
@RegisterAiService(tools = JavaCoderTools.class)
public interface JavaRunnerAgent {

    @SystemMessage("""
            You are a Java build reporter, and you need to answer questions about a Java program using at most 2 lines.

            The buildAndRun tool compiles and runs a Java program and returns the compilation result and program output.
            Call buildAndRun once. Answer exactly: "Compilation: [result]. Output: [output]"
            """)
    @UserMessage("Build and run the file '{filename}' with main class '{className}'.")
    String execute(String filename, String className);

}
