package io.retit.opentelemetry.javaagent.extension.commons.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import io.retit.opentelemetry.javaagent.extension.commons.NativeFacade;

/**
 * JNA Library which allows access to the native Windows API.
 * Generally, this class should not be accessed directly - the wrapper
 * functions defined in {@link NativeFacade} should be used instead.
 * Currently, we only define those methods which we need for our purposes.
 * If you need another method from the kernel32 API, just add the method
 * to the interface. However, please also add a generalized method
 * to {@link NativeFacade} if possible.
 */
@SuppressWarnings("PMD")
public interface Kernel32Library extends Library {

    /**
     * Singleton of the Kernel32Library.
     */
    Kernel32Library INSTANCE = Native.loadLibrary("kernel32", Kernel32Library.class);

    /**
     * Returns the process id of the current process.
     *
     * @return - process id.
     */
    @SuppressWarnings("checkstyle:methodName")
    int GetCurrentProcessId();

    /**
     * Returns the thread id of the current thread.
     *
     * @return - thread id.
     */
    @SuppressWarnings("checkstyle:methodName")
    int GetCurrentThreadId();
}
