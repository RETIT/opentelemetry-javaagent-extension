package io.retit.opentelemetry.javaagent.extension.resources.common;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.WinBase;
import io.retit.opentelemetry.javaagent.extension.resources.linux.LinuxCLibrary;
import io.retit.opentelemetry.javaagent.extension.resources.macos.MacOSSystemLibrary;
import io.retit.opentelemetry.javaagent.extension.resources.windows.WindowsKernel32Library;

/**
 * <code>NativeFacade</code> is a wrapper class which provides access to native
 * methods across platforms.
 * Some methods which we need for the RETIT OpenTelementry Extension are not accessible through Java API,
 * but instead must be accessed using native libraries. This class bundles all
 * such methods and provides implementations across multiple platforms. For this
 * purpose, JNA is used to consume the native C libraries.
 * Currently, the only supported platforms are Windows and Linux. In addition,
 * some of the methods are only applicable on certain platforms, e.g.
 * <code>getCurrentThreadCpuTime()</code>.
 */
public class NativeFacade {

    private NativeFacade() {
        // Utility class
    }

    /**
     * Gets the id of the current thread (tid).
     * This method only provides platform specific tids for Windows, Linux and Mac.
     * For other platforms it falls back on the Java tid.
     *
     * @return the tid of the thread
     */
    public static long getThreadId() {
        if (Platform.isWindows()) {
            return WindowsKernel32Library.INSTANCE.GetCurrentThreadId();
        } else if (Platform.isLinux() && Platform.isIntel()) {
            return LinuxCLibrary.INSTANCE.gettid();
        } else if (Platform.isMac()) {
            ThreadHandle handle = MacOSSystemLibrary.INSTANCE.pthread_self();
            return MacOSSystemLibrary.INSTANCE.pthread_mach_thread_np(handle);
        } else {
            return Thread.currentThread().getId();
        }
    }

    /**
     * Gets the amount of CPU time in ns consumed by the current thread.
     *
     * @return the CPU time in ns consumed by the current thread
     */
    public static long getCurrentThreadCpuTime() {
        if (Platform.isWindows()) {
            ThreadHandle threadHandle = WindowsKernel32Library.INSTANCE.GetCurrentThread();
            WinBase.FILETIME lpCreationTime = new WinBase.FILETIME();
            WinBase.FILETIME lpExitTime = new WinBase.FILETIME();
            WinBase.FILETIME lpKernelTime = new WinBase.FILETIME();
            WinBase.FILETIME lpUserTime = new WinBase.FILETIME();

            WindowsKernel32Library.INSTANCE.GetThreadTimes(threadHandle, lpCreationTime, lpExitTime, lpKernelTime, lpUserTime);

            long cpuTime = lpKernelTime.toDWordLong().longValue() + lpUserTime.toDWordLong().longValue();
            /**
             * All times are expressed using FILETIME data structures.
             * Such a structure contains two 32-bit values that combine to form a 64-bit count of 100-nanosecond time units.
             * https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-getthreadtimes
             */
            return cpuTime * 100;
        } else if (Platform.isLinux()) {
            return getTotalClockTime(LinuxCLibrary.INSTANCE, LinuxCLibrary.CLOCK_THREAD_CPUTIME_ID);
        } else if (Platform.isMac()) {
            return getTotalClockTime(MacOSSystemLibrary.INSTANCE, MacOSSystemLibrary.CLOCK_THREAD_CPUTIME_ID);
        }

        return 0L;
    }

    private static long getTotalClockTime(final CLibrary cLibrary, int clockId) {
        CLibrary.TimeSpec timeSpecBefore = new CLibrary.TimeSpec();
        cLibrary.clock_gettime(clockId, timeSpecBefore);
        return (timeSpecBefore.tv_sec.longValue() * 1_000_000_000l) + timeSpecBefore.tv_nsec;
    }
}
