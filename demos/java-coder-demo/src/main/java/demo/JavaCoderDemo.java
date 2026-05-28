package demo;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@QuarkusMain(name = "JavaCoderDemo")
public class JavaCoderDemo implements QuarkusApplication {

    private static final String PROMPT = "Write a Hello World Java program";

    private static final Logger LOG = Logger.getLogger(JavaCoderDemo.class);
    private static final Pattern CODE_BLOCK   = Pattern.compile("```(?:java)?\\s*\\n(.+?)```", Pattern.DOTALL);
    private static final Pattern PUBLIC_CLASS = Pattern.compile("public\\s+class\\s+(\\w+)");

    @Inject JavaCodeGeneratorAgent codeGenAgent;
    @Inject CodeWriterAgent codeWriterAgent;
    @Inject JavaRunnerAgent runnerAgent;

    @Override
    public int run(String... args) {
        String prompt = args.length > 0 ? String.join(" ", args) : PROMPT;

        System.out.println("Java Coder Demo");
        System.out.println("===============");
        System.out.println("Prompt: " + prompt);
        System.out.println();

        System.out.println("LLM Response (Code Generation):");
        System.out.println("-------------------------------");
        String response  = codeGenAgent.generateCode(prompt);
        System.out.println(response);
        System.out.println();

        String code      = extractCode(response);
        String className = extractClassName(code);
        String filename  = className + ".java";
        LOG.infof("[JavaCoder] generated %d chars → %s", code.length(), filename);

        System.out.println("LLM Response (Code Writing):");
        System.out.println("---------------------------");
        response = codeWriterAgent.writeFile(filename, code);
        System.out.println(response);
        System.out.println();

        System.out.println("LLM Response (Build & Run):");
        System.out.println("---------------------------");
        response = runnerAgent.execute(filename, className);
        System.out.println(response);
        System.out.println();
        return 0;
    }

    private static String extractCode(String response) {
        Matcher m = CODE_BLOCK.matcher(response);
        return m.find() ? m.group(1).strip() : response.strip();
    }

    private static String extractClassName(String code) {
        Matcher m = PUBLIC_CLASS.matcher(code);
        return m.find() ? m.group(1) : "Main";
    }

    public static void main(String[] args) {
        Quarkus.run(JavaCoderDemo.class, args);
    }
}
