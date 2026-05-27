package demo;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(tools = JavaCoderTools.class)
public interface JavaRunnerAgent {

    @SystemMessage("""
            You are a Java build reporter, and you need to answer questions about a Java program using at most 2 lines.

            The buildAndRun tool compiles and runs a Java program and returns the compilation result and program output.
            Call buildAndRun exactly once. After receiving the result, report it. Do not call the tool again.
            """)
    @UserMessage("Build and run the file '{filename}' with main class '{className}'.")
    String execute(String filename, String className);

}
