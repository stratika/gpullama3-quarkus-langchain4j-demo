package demo;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

@QuarkusMain(name = "StreamingSummarizationDemo")
public class StreamingSummarizationDemo implements QuarkusApplication {

    @Inject
    SummarizationStreamingService streamingService;

    @Override
    public int run(String... args) throws Exception {

        System.out.println("Summarization Demo (StreamingChatModel)");
        System.out.println("=======================================");

        try (InputStream is = StreamingSummarizationDemo.class.getClassLoader().getResourceAsStream("SampleTextToSummarize.txt")) {
            if (is == null) {
                throw new FileNotFoundException("SampleTextToSummarize.txt not found in resources");
            }

            String article = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            System.out.println("\nOriginal Article:\n=================\n" + article);
            System.out.println("\nStreaming Summary:\n===================");

            CountDownLatch done = new CountDownLatch(1);

            streamingService.summarize(article)
                    .subscribe()
                    .with(
                            chunk -> System.out.print(chunk),
                            failure -> {
                                failure.printStackTrace();
                                done.countDown();
                            },
                            done::countDown
                    );

            done.await();
            System.out.println("\n--- Streaming Complete ---");
        }

        return 0;
    }

    public static void main(String[] args) {
        Quarkus.run(StreamingSummarizationDemo.class, args);
    }
}
