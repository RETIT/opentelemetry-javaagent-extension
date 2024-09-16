package io.retit.opentelemetry.javaagent.extension.commons;

import com.sun.jna.Platform;
import io.retit.opentelemetry.javaagent.extension.commons.linux.CLibrary;
import io.retit.opentelemetry.javaagent.extension.commons.windows.Kernel32Library;

/**
 * <code>NativeFacade</code> is a wrapper class which provides access to native
 * methods across platforms.
 * Some methods which we need for RETIT APM are not accessible through Java API,
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
     * Gets the id of the current process (pid).
     *
     * @return the pid of the process
     */
    public static int getProcessId() {
        if (Platform.isWindows()) {
            return Kernel32Library.INSTANCE.GetCurrentProcessId();
        } else if (Platform.isLinux()) {
            return CLibrary.INSTANCE.getpid();
        }

        return 0;
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
}
