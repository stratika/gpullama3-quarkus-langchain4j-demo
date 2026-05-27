package demo;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Iterative Java coder: generates code, compiles via tool calling, and if
 * compilation fails asks the LLM to fix the error — up to MAX_FIX_ATTEMPTS.
 *
 * Agent design (weather-agent style):
 *   JavaExecutionAgent  — generates code as plain text       (CityExtractorAgent pattern)
 *   JavaRunnerAgent     — calls buildAndRun tool and reports  (WeatherForecastAgent pattern)
 *   CodeFixerAgent      — fixes code given a compiler error   (CityExtractorAgent pattern)
 *
 * The fix loop is host-controlled: JavaCoderTools.getLastBuildResult() exposes the
 * raw tool result so the host can decide whether to fix without calling the tool twice.
 */
@QuarkusMain(name = "JavaCoderIterativeDemo")
public class JavaCoderIterativeDemo implements QuarkusApplication {

    private static final String  PROMPT           = "Write a matrix multiplication Java program";
    private static final int     MAX_FIX_ATTEMPTS = 3;

    private static final Logger  LOG          = Logger.getLogger(JavaCoderIterativeDemo.class);
    private static final Pattern CODE_BLOCK   = Pattern.compile("```(?:java)?\\s*\\n(.+?)```", Pattern.DOTALL);
    private static final Pattern PUBLIC_CLASS = Pattern.compile("public\\s+class\\s+(\\w+)");

    @Inject JavaExecutionAgent codeGen;
    @Inject JavaRunnerAgent    runner;
    @Inject CodeFixerAgent     fixer;
    @Inject JavaCoderTools     tools;

    @Override
    public int run(String... args) {
        String prompt = args.length > 0 ? String.join(" ", args) : PROMPT;

        System.out.println("Java Coder (Iterative) Demo");
        System.out.println("===========================");
        System.out.println("Prompt: " + prompt);
        System.out.println();

        // Phase 1: model generates initial Java source
        String response  = codeGen.generateCode(prompt);
        String code      = extractCode(response);
        String className = extractClassName(code);
        String filename  = className + ".java";
        LOG.infof("[JavaCoder] generated %d chars → %s", code.length(), filename);

        // Phase 2+3: runner agent calls buildAndRun tool; on failure ask fixer and retry
        for (int attempt = 1; attempt <= MAX_FIX_ATTEMPTS; attempt++) {

            System.out.printf("%n[Attempt %d/%d]%n", attempt, MAX_FIX_ATTEMPTS);
            tools.writeFile(filename, code);

            // Tool calling via runner agent — produces [Tool turn] / [LLM → tool call] logs
            String report = runner.execute(filename, className);

            // Host checks the raw result captured by JavaCoderTools (no second tool call)
            String rawResult = tools.getLastBuildResult();

            if (rawResult == null || rawResult.startsWith("Compilation FAILED:")) {
                System.out.println(rawResult);
            } else {
                // Compilation succeeded — report the runner agent's synthesis and stop
                // (runtime exceptions are a logic issue outside the scope of the fixer)
                System.out.println(report);
                return 0;
            }

            if (attempt == MAX_FIX_ATTEMPTS) {
                System.out.printf("%nFailed after %d attempts. Giving up.%n", MAX_FIX_ATTEMPTS);
                return 1;
            }

            // Phase 3: ask LLM to fix the code based on the compiler error
            String error = rawResult != null
                    ? rawResult.substring("Compilation FAILED:\n".length())
                    : "Unknown compilation error";
            LOG.infof("[JavaCoder] attempt %d failed — asking LLM to fix", attempt);
            String fixResponse = fixer.fixCode(code, error);
            code      = extractCode(fixResponse);
            className = extractClassName(code);
            filename  = className + ".java";
            LOG.infof("[JavaCoder] fixed code: %d chars → %s", code.length(), filename);
        }

        return 1;
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
        Quarkus.run(JavaCoderIterativeDemo.class, args);
    }
}
