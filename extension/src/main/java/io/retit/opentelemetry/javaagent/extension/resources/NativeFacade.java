package io.retit.opentelemetry.javaagent.extension.resources;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.WinBase;
import io.retit.opentelemetry.javaagent.extension.resources.linux.CLibrary;
import io.retit.opentelemetry.javaagent.extension.resources.windows.Kernel32Library;

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

    @SuppressWarnings("PMD")
    private static long CLOCK_TICKS_LINUX = -1;

    private NativeFacade() {
        // Utility class
    }

    /**
     * Gets the id of the current thread (tid).
     * On Linux, this method currently only works for x86 and x86_64 platforms
     * since the IDs for the syscall are architecture-specific. If support for
     * other platforms is desired, the respective syscall IDs need to be looked
     * up and added to {@link CLibrary}.
     *
     * @return the tid of the thread
     */
    public static int getThreadId() {
        if (Platform.isWindows()) {
            return Kernel32Library.INSTANCE.GetCurrentThreadId();
        } else if (Platform.isLinux() && Platform.isIntel()) {
            if (Platform.is64Bit()) {
                return CLibrary.INSTANCE.syscall(CLibrary.GETTID_X86_64);
            } else {
                return CLibrary.INSTANCE.syscall(CLibrary.GETTID_X86_32);
            }
        }

        return 0;
    }

    /**
     * Returns the clock ticks configured on linux.
     *
     * @return clock ticks configured on linux
     */
    public static long getClockTicks() {

        if (CLOCK_TICKS_LINUX == -1 && Platform.isLinux()) {
            CLOCK_TICKS_LINUX = CLibrary.INSTANCE.sysconf(CLibrary._SC_CLK_TCK);
        }

        return CLOCK_TICKS_LINUX;
    }

    /**
     * Gets the amount of CPU time in ns consumed by the current thread.
     *
     * @return the CPU time in ns consumed by the current thread
     */
    public static long getCurrentThreadCpuTime() {
        if (Platform.isWindows()) {
            Kernel32Library.Handle threadHandle = Kernel32Library.INSTANCE.GetCurrentThread();
            WinBase.FILETIME lpCreationTime = new WinBase.FILETIME();
            WinBase.FILETIME lpExitTime = new WinBase.FILETIME();
            WinBase.FILETIME lpKernelTime = new WinBase.FILETIME();
            WinBase.FILETIME lpUserTime = new WinBase.FILETIME();

            Kernel32Library.INSTANCE.GetThreadTimes(threadHandle, lpCreationTime, lpExitTime, lpKernelTime, lpUserTime);

            long cpuTime = lpKernelTime.toDWordLong().longValue() + lpUserTime.toDWordLong().longValue();
            /**
             * All times are expressed using FILETIME data structures.
             * Such a structure contains two 32-bit values that combine to form a 64-bit count of 100-nanosecond time units.
             * https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-getthreadtimes
             */
            return cpuTime * 100;
        }

        return 0L;
    }
}
