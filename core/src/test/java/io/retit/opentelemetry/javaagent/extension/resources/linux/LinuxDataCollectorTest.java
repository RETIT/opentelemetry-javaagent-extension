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
            Assertions.assertNotEquals(0, nativeThreadId);
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

    @Test
    public void testDiskBytesAreParsedFromReadBytesAndWriteBytes() {
        // rchar and wchar include socket, pipe and terminal I/O and must not be used as storage demand
        String procFsIoContent = "rchar: 476726516\n"
                + "wchar: 450053132\n"
                + "syscr: 1145703\n"
                + "syscw: 461006\n"
                + "read_bytes: 933888\n"
                + "write_bytes: 26984448\n"
                + "cancelled_write_bytes: 4096\n";

        Assertions.assertArrayEquals(new long[]{933888, 26984448}, LinuxDataCollector.parseDiskBytesReadAndWritten(procFsIoContent));
    }

    @Test
    public void testDiskBytesAreZeroIfProcFsValuesAreMissing() {
        // e.g. kernels without CONFIG_TASK_IO_ACCOUNTING only provide rchar, wchar, syscr and syscw
        Assertions.assertArrayEquals(new long[]{0, 0}, LinuxDataCollector.parseDiskBytesReadAndWritten("rchar: 476726516\nwchar: 450053132\n"));
    }

    @Test
    public void testCancelledWriteBytesAreNotUsedAsWriteBytes() {
        String procFsIoContent = "rchar: 1\nwchar: 2\nsyscr: 3\nsyscw: 4\nread_bytes: 5\ncancelled_write_bytes: 7\nwrite_bytes: 6";

        Assertions.assertArrayEquals(new long[]{5, 6}, LinuxDataCollector.parseDiskBytesReadAndWritten(procFsIoContent));
    }

    @Test
    public void testDiskBytesReadAndWrittenLinux() {
        if (Platform.isLinux()) {
            long[] diskBytes = new LinuxDataCollector().getDiskBytesReadAndWritten();
            Assertions.assertEquals(2, diskBytes.length);
            Assertions.assertTrue(diskBytes[0] >= 0);
            Assertions.assertTrue(diskBytes[1] >= 0);
        }
    }
}
