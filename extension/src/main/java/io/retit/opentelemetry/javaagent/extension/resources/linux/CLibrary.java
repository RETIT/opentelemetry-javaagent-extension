package io.retit.opentelemetry.javaagent.extension.resources.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * JNA Library which allows access to the native C API of Linux.
 * Generally, this class should not be accessed directly - the wrapper
 * functions defined in {@link io.retit.opentelemetry.javaagent.extension.resources.NativeFacade} should be used instead.
 * Currently, we only define those methods which we need for our purposes.
 * If you need another method from the glibc API, just add the method
 * to the interface. However, please also add a generalized method
 * to {@link io.retit.opentelemetry.javaagent.extension.resources.NativeFacade} if possible.
 */
@SuppressWarnings("PMD")
public interface CLibrary extends Library {

    /**
     * The instance provided by the JNA library to interact with the native c library.
     */
    CLibrary INSTANCE = Native.loadLibrary("c", CLibrary.class);

    /**
     * System call number for getting the current thread ID on 32bit systems.
     * Derived from Linux/arch/x86/include/asm/unistd_32.h
     */
    int GETTID_X86_32 = 224;

    /**
     * System call number for getting the current thread ID on 64bit systems.
     * Derived from Linux/arch/x86/include/asm/unistd_64.h
     */
    int GETTID_X86_64 = 186;

    /**
     * System config name to fetch the number of clock ticks per second.
     * See https://man7.org/linux/man-pages/man3/sysconf.3.html.
     */
    @SuppressWarnings("checkstyle:ConstantName")
    int _SC_CLK_TCK = 2;

    /**
     * Fetches the process id of the calling process.
     *
     * @return - the process Id.
     */
    int getpid();

    /**
     * Executes a system call (interrupt) with the given number.
     *
     * @param syscall - syscall number
     * @return - result of the syscall.
     */
    int syscall(int syscall);

    /**
     * Fetches the system configuration value with the given name.
     *
     * @param name - the configuration for which the configuration needs to be read.
     * @return - the configuration value.
     */
    long sysconf(int name);
}