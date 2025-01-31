package io.retit.opentelemetry.javaagent.extension.resources.windows;

import com.sun.jna.Platform;
import io.retit.opentelemetry.javaagent.extension.resources.common.NativeFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WindowsDataCollectorTest {

    @Test
    public void testNativeThreadIdWindows() {
        if (Platform.isWindows()) {
            long javaThreadId = Thread.currentThread().getId();
            long nativeThreadId = NativeFacade.getThreadId();
            Assertions.assertNotEquals(javaThreadId, nativeThreadId);
        }
    }

    @Test
    public void testPlatformSpecificThreadCpuTimeWindows() {
        if (Platform.isWindows()) {
            WindowsDataCollector windowsDataCollector = new WindowsDataCollector();
            long currentThreadCpuTimeThreadQueryThreadTime = windowsDataCollector.getPlatformSpecificThreadCpuTime();
            long currentThreadCpuTimeThreadMXBean = windowsDataCollector.getCurrentThreadCpuTime();

            long difference = currentThreadCpuTimeThreadMXBean - currentThreadCpuTimeThreadQueryThreadTime;
            // assert that the measurements are less than 20ms different (as they consume cpu time themselves)
            Assertions.assertTrue(difference < 20_000_000);
        }
    }

}
