package demo;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface CodeFixerAgent {

    @UserMessage("""
            The following Java code failed to compile with this error:

            Error:
            {error}

            Note: "reached end of file while parsing" means the last closing brace } of the class is missing.
            Add the missing } at the end of the code.

            Current code:
            {code}

            Only reply with the complete fixed Java source code inside a ```java code block.
            """)
    String fixCode(String code, String error);

}
