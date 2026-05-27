package demo;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class DirectoryTools {

    @Tool("List the contents of an absolute directory path. Use this whenever the user asks what is inside a directory.")
    public String listDirectory(
            @P("Absolute directory path, for example /tmp or /home/orion/Desktop")
            String path) {

        if (path == null || path.isBlank()) {
            return "Error: path must not be empty.";
        }

        Path dir;
        try {
            dir = Path.of(path).normalize();
        } catch (Exception e) {
            return "Error: invalid path: " + path;
        }

        if (!dir.isAbsolute()) {
            return "Error: path must be absolute. Got: " + path;
        }
        if (!Files.exists(dir)) {
            return "Error: path does not exist: " + dir;
        }
        if (!Files.isDirectory(dir)) {
            return "Error: path is not a directory: " + dir;
        }

        try (var stream = Files.list(dir).sorted()) {
            List<Path> entries = stream.collect(Collectors.toList());

            if (entries.isEmpty()) {
                return "Contents of " + dir + ":\n(empty directory)";
            }

            StringBuilder sb = new StringBuilder("Contents of ").append(dir).append(":\n");
            for (Path p : entries) {
                if (Files.isDirectory(p)) {
                    sb.append("dir:  ").append(p.getFileName()).append("\n");
                } else {
                    try {
                        long size = Files.size(p);
                        sb.append("file: ").append(p.getFileName()).append(", ").append(size).append(" bytes\n");
                    } catch (IOException e) {
                        sb.append("file: ").append(p.getFileName()).append("\n");
                    }
                }
            }
            return sb.toString();
        } catch (IOException e) {
            return "Error listing directory: " + e.getMessage();
        }
    }
}