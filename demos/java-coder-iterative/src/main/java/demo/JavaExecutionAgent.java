package demo;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface JavaExecutionAgent {

    @UserMessage("""
            Write a complete, compilable Java program for the following request.
            Only reply with the Java source code inside a ```java code block.
            Always include a public class with a main method.

            Request: {request}
            """)
    String generateCode(String request);

}
