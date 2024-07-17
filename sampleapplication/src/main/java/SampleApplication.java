import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.Random;
import java.util.logging.Logger;

public class SampleApplication {

    private static final Logger LOGGER = Logger.getLogger(SampleApplication.class.getName());
    private static final Random random = new Random();

    @WithSpan
    public static void method1() {
        LOGGER.info("inside method1");

    }

    @WithSpan
    public static void method2() {
        LOGGER.info("method2");
        try {
            Thread.sleep(random.nextInt(2000)); // Random delay between 0 to 2000 milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning("Thread interrupted");
        }
    }

    public static void main(String[] args) {
       // for (int i = 0; i < 1; i++) { // Simulate 10 requests
            Span span = Span.current();
            span.setAttribute("test", "some value");
            method1();
            //method2();
            span.end();
            System.out.println("just printing something");
        //}
    }
}