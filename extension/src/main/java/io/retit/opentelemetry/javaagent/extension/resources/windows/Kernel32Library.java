package io.retit.opentelemetry.javaagent.extension.resources.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.ptr.LongByReference;

/**
 * JNA Library which allows access to the native Windows API.
 * <p>
 * Generally, this class should not be accessed directly - the wrapper
 * functions defined in {@link io.retit.opentelemetry.javaagent.extension.resources.NativeFacade} should be used instead.
 * <p>
 * Currently, we only define those methods which we need for our purposes.
 * If you need another method from the kernel32 API, just add the method
 * to the interface. However, please also add a generalized method
 * to {@link io.retit.opentelemetry.javaagent.extension.resources.NativeFacade} if possible.
 */
public interface Kernel32Library extends Library {
    Kernel32Library INSTANCE = Native.loadLibrary("kernel32", Kernel32Library.class);

    //CHECKSTYLE:OFF
    int GetCurrentProcessId();

    int GetCurrentThreadId();

    Handle GetCurrentThread();

    boolean QueryThreadCycleTime(Handle threadHandle, LongByReference cycles);

    /**
     * see https://learn.microsoft.com/en-en/windows/win32/api/processthreadsapi/nf-processthreadsapi-getthreadtimes.
     */
    boolean GetThreadTimes(Handle threadHandle, WinBase.FILETIME lpCreationTime, WinBase.FILETIME lpExitTime, WinBase.FILETIME lpKernelTime, WinBase.FILETIME lpUserTime);
    //CHECKSTYLE:ON

    class Handle extends PointerType {
        public Handle(Pointer address) {
            super(address);
        }

        public Handle() {
            super();
        }
    }
}
