package io.retit.opentelemetry.javaagent.extension.resources.linux;

import com.sun.jna.Platform;
import io.retit.opentelemetry.javaagent.extension.resources.common.NativeFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LinuxDataCollectorTest {

    @Test
    public void testNativeThreadIdLinux() {
        if(Platform.isLinux()) {
            long javaThreadId = Thread.currentThread().getId();
            long nativeThreadId = NativeFacade.getThreadId();
            Assertions.assertNotEquals(javaThreadId, nativeThreadId);
        }
    }

    @Test
    public void testPlatformSpecificThreadCpuTimeLinux() {
        if (Platform.isLinux()) {
            LinuxDataCollector linuxDataCollector = new LinuxDataCollector();
            // call it once as after the first invocation the clock tick configuration is cached
            linuxDataCollector.getPlatformSpecificThreadCpuTime();
            long currentThreadCpuTimeThreadProcFS = linuxDataCollector.getPlatformSpecificThreadCpuTime();
            long currentThreadCpuTimeThreadMXBean = linuxDataCollector.getCurrentThreadCpuTime();

            long difference = currentThreadCpuTimeThreadMXBean - currentThreadCpuTimeThreadProcFS;
            // assert that the measurements are less than 2ms different (as they consume cpu time themselves)
            Assertions.assertTrue(difference < 2_000_000);
        }
    }
}
