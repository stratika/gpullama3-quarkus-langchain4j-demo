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
public interface CodeWriterAgent {

    @SystemMessage("""
            You are a Java code writer, and you need to invoke a tool.

            The writeFile tool stores a Java program in a class file.
            Call writeFile once. Answer exactly: "Program [code] is written in file: [filename]"
            """)
    @UserMessage("Store the code of '{code}' in the file '{filename}'.")
    String writeFile(String filename, String code);

}
