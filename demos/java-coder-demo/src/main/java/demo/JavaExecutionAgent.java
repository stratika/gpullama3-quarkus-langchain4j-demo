package demo;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Generates Java source code from a natural-language request.
 * Mirrors CityExtractorAgent: a focused single-purpose service with no tools
 * whose entire instruction lives in the @UserMessage.
 */
@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface JavaExecutionAgent {

    @UserMessage("""
            Write a complete, compilable Java program for the following request.
            Only reply with the Java source code inside a ```java code block.
            Always include a public class with a main method.
            Close every opening brace { with a matching closing brace } including the class closing brace.

            Request: {request}
            """)
    String generateCode(String request);

}
