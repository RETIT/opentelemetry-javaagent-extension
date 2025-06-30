package io.retit.opentelemetry.javaagent.extension.resources.linux;

import com.sun.jna.Native;
import io.retit.opentelemetry.javaagent.extension.resources.common.CLibrary;
import io.retit.opentelemetry.javaagent.extension.resources.common.NativeFacade;

/**
 * JNA Library which allows access to the native C API of Linux.
 * Generally, this class should not be accessed directly - the wrapper
 * functions defined in {@link NativeFacade} should be used instead.
 * Currently, we only define those methods which we need for our purposes.
 * If you need another method from the glibc API, just add the method
 * to the interface. However, please also add a generalized method
 * to {@link NativeFacade} if possible.
 */
public interface LinuxCLibrary extends CLibrary {
    /**
     * This is a clock that measures CPU time consumed by this
     * thread.
     */
    int CLOCK_THREAD_CPUTIME_ID = 3;

    /**
     * The instance provided by the JNA library to interact with the native c library.
     */
    LinuxCLibrary INSTANCE = Native.loadLibrary("c", LinuxCLibrary.class);

    /**
     * Fetches the thread id of the calling thread.
     *
     * @return - the thread Id.
     */
    int gettid();
}