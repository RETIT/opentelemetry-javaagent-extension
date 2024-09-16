package io.retit.opentelemetry.javaagent.extension.commons.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import io.retit.opentelemetry.javaagent.extension.commons.NativeFacade;

/**
 * JNA Library which allows access to the native C API of Linux.
 *
 * <p>Generally, this class should not be accessed directly - the wrapper
 * functions defined in {@link NativeFacade} should be used instead.
 *
 * <p>Currently, we only define those methods which we need for our purposes.
 * If you need another method from the glibc API, just add the method
 * to the interface. However, please also add a generalized method
 * to {@link NativeFacade} if possible.
 */
public interface CLibrary extends Library {

    /**
     * Singleton of the CLibrary.
     */
    CLibrary INSTANCE = Native.loadLibrary("c", CLibrary.class);

    /**
     * Interrupt number, derived from Linux/arch/x86/include/asm/unistd_32.h.
     */
    int GETTID_X86_32 = 224;

    /**
     * Interrupt number, derived from Linux/arch/x86/include/asm/unistd_64.h.
     */
    int GETTID_X86_64 = 186;

    /**
     * Returns the process id of the current process.
     *
     * @return - the process id of the current process.
     */
    int getpid();

    /**
     * Executes the given interrupt number as system call.
     *
     * @param interruptNumber - the interrupt that should be executed by the Kernel.
     * @return - the result of the system call.
     */
    int syscall(int interruptNumber);
}