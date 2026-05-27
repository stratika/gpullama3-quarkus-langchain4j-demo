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

    @Inject JavaExecutionAgent codeGen;
    @Inject JavaRunnerAgent    runner;
    @Inject JavaCoderTools     tools;

    @Override
    public int run(String... args) {
        String prompt = args.length > 0 ? String.join(" ", args) : PROMPT;

        System.out.println("Java Coder Demo");
        System.out.println("===============");
        System.out.println("Prompt: " + prompt);
        System.out.println();

        String response  = codeGen.generateCode(prompt);
        String code      = extractCode(response);
        String className = extractClassName(code);
        String filename  = className + ".java";
        LOG.infof("[JavaCoder] generated %d chars → %s", code.length(), filename);

        tools.writeFile(filename, code);

        System.out.println("Result:");
        System.out.println("-------");
        System.out.println(runner.execute(filename, className));
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
