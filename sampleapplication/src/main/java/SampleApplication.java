import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class SampleApplication {

    private static final Logger LOGGER = Logger.getLogger(SampleApplication.class.getName());
    private static final Random random = new Random();

    @WithSpan
    public static void method1() {
        LOGGER.info("inside method1");
        method2();
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
        method3();
    }

    @WithSpan
    public static void method3() {
        LOGGER.info("method3");
        try {
            Thread.sleep(random.nextInt(2000)); // Random delay between 0 to 2000 milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning("Thread interrupted");
        }
    }

    @WithSpan
    public static void main(String[] args) {
            Span span = Span.current();
            span.setAttribute("test", "some value");
            method1();
            span.end();
        System.out.println("Hello, World!");
    }
}