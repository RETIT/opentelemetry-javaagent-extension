import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.logging.Logger;

public class SampleApplication {

    private static final Logger LOGGER = Logger.getLogger(SampleApplication.class.getName());

    @WithSpan
    public static void method1() {
        LOGGER.info("method1");
    }

    @WithSpan
    public static void method2() {
        LOGGER.info("method2");
    }

    public static void main(String[] args) {
        // Call methods
        Span span = Span.current();
        span.setAttribute("test", "some value");
        method1();
        method2();
        span.end();
    }
}