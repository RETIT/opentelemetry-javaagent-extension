package io.retit.opentelemetry.javaagent.extension.resources;

import com.sun.jna.Platform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NativeFacadeTest {

    @Test
    public void testQueryThreadTimeWindows() {
        if (Platform.isWindows()) {
            WindowsDataCollector windowsDataCollector = new WindowsDataCollector();
            long currentThreadCpuTimeThreadQueryThreadTime = NativeFacade.getCurrentThreadCpuTime();
            long currentThreadCpuTimeThreadMXBean = windowsDataCollector.getCurrentThreadCpuTime();

            long difference = currentThreadCpuTimeThreadMXBean - currentThreadCpuTimeThreadQueryThreadTime;
            // assert that the measurements are less than 20ms different (as they consume cpu time themselves)
            Assertions.assertTrue(difference < 20_000_000);
        }
    }

}
