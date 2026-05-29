package demo;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@QuarkusMain(name = "SummarizationDemo")
public class SummarizationDemo implements QuarkusApplication {

    @Inject
    SummarizationService ai;

    @Override
    public int run(String... args) throws Exception {

        System.out.println("Summarization Demo (Regular ChatModel)");
        System.out.println("======================================");

        try (InputStream is = SummarizationDemo.class.getClassLoader().getResourceAsStream("SampleTextToSummarize.txt")) {
            if (is == null) {
                throw new FileNotFoundException("SampleTextToSummarize.txt not found in resources");
            }

            String article = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            System.out.println("\nOriginal Article:\n=================\n" + article);

            System.out.println("\nSummary:\n========\n" + ai.summarize(article));
        }

        return 0;
    }

    public static void main(String[] args) {
        Quarkus.run(SummarizationDemo.class, args);
    }
}
