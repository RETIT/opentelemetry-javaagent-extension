package io.retit.opentelemetry.javaagent.extension.resources.macos;

import com.sun.jna.Platform;
import io.retit.opentelemetry.javaagent.extension.resources.common.NativeFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MacOSDataCollectorTest {

    @Test
    public void testNativeThreadIdMacOS() {
        if(Platform.isMac()) {
            long javaThreadId = Thread.currentThread().getId();
            long nativeThreadId = NativeFacade.getThreadId();
            Assertions.assertNotEquals(0, nativeThreadId);
            Assertions.assertNotEquals(javaThreadId, nativeThreadId);
        }
    }

    @Test
    public void testPlatformSpecificThreadCpuTimeMacOS() {
        if (Platform.isMac()) {
            MacOSDataCollector macosDataCollector = new MacOSDataCollector();
            // call it once as after the first invocation the clock tick configuration is cached
            macosDataCollector.getPlatformSpecificThreadCpuTime();
            long currentThreadCpuTimeThreadProcFS = macosDataCollector.getPlatformSpecificThreadCpuTime();
            long currentThreadCpuTimeThreadMXBean = macosDataCollector.getCurrentThreadCpuTime();

            long difference = currentThreadCpuTimeThreadMXBean - currentThreadCpuTimeThreadProcFS;
            // assert that the measurements are less than 2ms different (as they consume cpu time themselves)
            Assertions.assertTrue(difference < 2_000_000);
        }
    }
}
