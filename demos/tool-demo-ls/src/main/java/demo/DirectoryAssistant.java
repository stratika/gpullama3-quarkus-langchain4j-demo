package demo;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface DirectoryAssistant {

    @SystemMessage("""
        You are a directory listing assistant.

        You have one tool: listDirectory(path).
        The tool returns a listing in this format:
          "Contents of /some/path:\\ndir:  dirname\\nfile: filename, N bytes\\n..."

        When the user asks about a directory:
        1. Call listDirectory once with that path.
        2. Print each line from the tool result exactly as returned.

        Do not describe the tool call. Do not summarize. Print the actual entries.
        """)
    @UserMessage("{{it}}")
    @ToolBox(DirectoryTools.class)
    String ask(String prompt);
}
