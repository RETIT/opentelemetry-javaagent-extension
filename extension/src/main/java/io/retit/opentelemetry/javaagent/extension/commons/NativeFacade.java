package io.retit.opentelemetry.javaagent.extension.commons;

import com.sun.jna.Platform;
import com.sun.jna.ptr.LongByReference;
import io.retit.opentelemetry.javaagent.extension.commons.windows.Kernel32Library;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>NativeFacade</code> is a wrapper class which provides access to native
 * methods across platforms.
 * <p>
 * Some methods which we need for RETIT APM are not accessible through Java API,
 * but instead must be accessed using native libraries. This class bundles all
 * such methods and provides implementations across multiple platforms. For this
 * purpose, JNA is used to consume the native C libraries.
 * <p>
 * Currently, the only supported platforms are Windows and Linux. In addition,
 * some of the methods are only applicable on certain platforms, e.g.
 * <code>getCurrentThreadCpuTime()</code>.
 *

 */
public class NativeFacade {

    private static final Logger LOGGER = Logger.getLogger(NativeFacade.class.getName());

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
     * <p>
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
     * Gets the amount of CPU cycles consumed by the current thread.
     * <p>
     * This method is only applicable on Windows platforms.
     *
     * @return
     */
    public static long getCurrentThreadCpuTime() {
        if (Platform.isWindows()) {
            Kernel32Library.Handle threadHandle = Kernel32Library.INSTANCE.GetCurrentThread();
            LongByReference cycles = new LongByReference();
            Kernel32Library.INSTANCE.QueryThreadCycleTime(threadHandle, cycles);
            return cycles.getValue();
        }

        return 0L;
    }

    /**
     * Gets the model name of this system's CPU.
     * <p>
     * Currently, this is only implemented for Linux.
     *
     * @return
     */
    public static String getCpuModel() {
        if (Platform.isLinux()) {
            try {
                return CpuInfoParser.getEntry("model name");
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return null;
    }
}
