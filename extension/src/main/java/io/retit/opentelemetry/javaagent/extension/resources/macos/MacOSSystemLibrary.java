package io.retit.opentelemetry.javaagent.extension.resources.macos;

import com.sun.jna.Native;
import io.retit.opentelemetry.javaagent.extension.resources.common.CLibrary;
import io.retit.opentelemetry.javaagent.extension.resources.common.NativeFacade;
import io.retit.opentelemetry.javaagent.extension.resources.common.ThreadHandle;

/**
 * JNA Library which allows access to the native System API of MacOS.
 * Generally, this class should not be accessed directly - the wrapper
 * functions defined in {@link NativeFacade} should be used instead.
 * Currently, we only define those methods which we need for our purposes.
 * If you need another method from the glibc API, just add the method
 * to the interface. However, please also add a generalized method
 * to {@link NativeFacade} if possible.
 */
@SuppressWarnings("PMD")
public interface MacOSSystemLibrary extends CLibrary {

    MacOSSystemLibrary INSTANCE = Native.load("System", MacOSSystemLibrary.class);

    ThreadHandle pthread_self();

    int pthread_threadid_np(ThreadHandle threadHandle, Long thread_id);
}
