package demo;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

@QuarkusMain(name = "LsToolDemo")
public class LsToolDemo implements QuarkusApplication {

    private static final String DEFAULT_PROMPT = "Show me what is inside /tmp";

    @Inject
    DirectoryAssistant ai;

    @Override
    public int run(String... args) {
        //System.out.println(ai.ask("Just to Warmup yourself"));
        String prompt = (args.length > 0) ? String.join(" ", args) : DEFAULT_PROMPT;

        System.out.println("Tool Calling Demo — listDirectory");
        System.out.println("==================================");
        System.out.println("Prompt: " + prompt);
        System.out.println();

//        System.out.println("Answer:");
//        System.out.println("-------");
        System.out.println(ai.ask(prompt));

        return 0;
    }

    public static void main(String[] args) {
        Quarkus.run(LsToolDemo.class, args);
    }
}
