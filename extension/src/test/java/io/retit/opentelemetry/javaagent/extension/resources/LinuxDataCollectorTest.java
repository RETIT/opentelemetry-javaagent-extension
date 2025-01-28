package io.retit.opentelemetry.javaagent.extension.resources;

import com.sun.jna.Platform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LinuxDataCollectorTest {

    @Test
    public void testProcFSThreadTimeLinux() {
        if (Platform.isLinux()) {
            LinuxDataCollector linuxDataCollector = new LinuxDataCollector();
            // call it once as after the first invocation the clock tick configuration is cached
            linuxDataCollector.getPlatformSpecificThreadCpuTime();
            long currentThreadCpuTimeThreadProcFS = linuxDataCollector.getPlatformSpecificThreadCpuTime();
            long currentThreadCpuTimeThreadMXBean = linuxDataCollector.getCurrentThreadCpuTime();

            long difference = currentThreadCpuTimeThreadMXBean - currentThreadCpuTimeThreadProcFS;
            // assert that the measurements are less than 15ms different (as they consume cpu time themselves)
            Assertions.assertTrue(difference < 15_000_000);
        }
    }

    @Test
    public void testParsingOfProcFSStatData() {
        String exampleData = "76794 (java) R 26801 76783 76783 0 -1 4194368 7527 570 304 0 172 119 0 0 20 0 26 0 9727802" +
                " 7644819456 25129 18446744073709551615 94808403058688 94808403059753 140728768974160 0 0 0 4 0 16800975" +
                " 0 0 0 -1 2 0 0 0 0 0 94808403070272 94808403071000 94809337655296 140728768979617 140728768985464" +
                " 140728768985464 140728768987084 0";
        LinuxDataCollector linuxDataCollector = new LinuxDataCollector();
        long expectedUserCycles = 172;
        long expectedSystemCycles = 119;
        long combinedData = linuxDataCollector.getCombinedUserAndSystemClockTicks(exampleData);
        Assertions.assertEquals(expectedUserCycles + expectedSystemCycles, combinedData);
    }
}
