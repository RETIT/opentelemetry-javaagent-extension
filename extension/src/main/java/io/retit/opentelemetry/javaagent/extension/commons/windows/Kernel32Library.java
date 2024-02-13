package io.retit.opentelemetry.javaagent.extension.commons.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.LongByReference;
import io.retit.opentelemetry.javaagent.extension.commons.NativeFacade;

/**
 * JNA Library which allows access to the native Windows API.
 * <p>
 * Generally, this class should not be accessed directly - the wrapper
 * functions defined in {@link NativeFacade} should be used instead.
 * <p>
 * Currently, we only define those methods which we need for our purposes.
 * If you need another method from the kernel32 API, just add the method
 * to the interface. However, please also add a generalized method
 * to {@link NativeFacade} if possible.
 */
@SuppressWarnings("PMD")
public interface Kernel32Library extends Library {
    Kernel32Library INSTANCE = Native.loadLibrary("kernel32", Kernel32Library.class);

    //CHECKSTYLE:OFF
    int GetCurrentProcessId();

    int GetCurrentThreadId();

    Handle GetCurrentThread();

    boolean QueryThreadCycleTime(Handle threadHandle, LongByReference cycles);
    //CHECKSTYLE:ON

    public static class Handle extends PointerType {
        public Handle(Pointer address) {
            super(address);
        }

        public Handle() {
            super();
        }
    }
}
