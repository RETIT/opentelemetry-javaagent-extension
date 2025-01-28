package io.retit.opentelemetry.javaagent.extension.resources.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.ptr.LongByReference;

/**
 * JNA Library which allows access to the native Windows API.
 * Generally, this class should not be accessed directly - the wrapper
 * functions defined in {@link io.retit.opentelemetry.javaagent.extension.resources.NativeFacade} should be used instead.
 * Currently, we only define those methods which we need for our purposes.
 * If you need another method from the kernel32 API, just add the method
 * to the interface. However, please also add a generalized method
 * to {@link io.retit.opentelemetry.javaagent.extension.resources.NativeFacade} if possible.
 */
@SuppressWarnings("PMD")
public interface Kernel32Library extends Library {
    /**
     * The instance provided by the JNA library to interact with the native kernel 32 library.
     */
    Kernel32Library INSTANCE = Native.loadLibrary("kernel32", Kernel32Library.class);

    /**
     * Retrieves the process identifier of the calling process.
     * See https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-getcurrentprocessid.
     *
     * @return The process id of the calling process.
     */
    int GetCurrentProcessId();

    /**
     * Retrieves the thread identifier of the calling thread.
     * See https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-getcurrentthreadid.
     *
     * @return The thread id of the calling thread.
     */
    int GetCurrentThreadId();

    /**
     * Retrieves a handle for the current thread.
     * See https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-getcurrentthread.
     *
     * @return a handle for the current thread.
     */
    Handle GetCurrentThread();

    /**
     * Retrieves the cycle time for the specified thread.
     * See https://learn.microsoft.com/en-us/windows/win32/api/realtimeapiset/nf-realtimeapiset-querythreadcycletime.
     *
     * @param threadHandle - A handle to the thread.
     * @param cycles       - Ths reference will contain the number of CPU clock cycles used by the thread (incl. Kernel and User mode).
     * @return - a boolean indicator if the call was successful, the return value is in the cycles reference.
     */
    boolean QueryThreadCycleTime(Handle threadHandle, LongByReference cycles);

    /**
     * Retrieves the cycle time for the specified thread.
     * See https://learn.microsoft.com/en-en/windows/win32/api/processthreadsapi/nf-processthreadsapi-getthreadtimes.
     *
     * @param threadHandle   - A handle to the thread.
     * @param lpCreationTime - A pointer to a FILETIME structure that receives the creation time of the thread.
     * @param lpExitTime     - A pointer to a FILETIME structure that receives the exit time of the thread.
     * @param lpKernelTime   - A pointer to a FILETIME structure that receives the amount of time that the thread has executed in kernel mode.
     * @param lpUserTime     - A pointer to a FILETIME structure that receives the amount of time that the thread has executed in user mode.
     * @return - a boolean indicator if the call was successful.
     */
    boolean GetThreadTimes(Handle threadHandle, WinBase.FILETIME lpCreationTime, WinBase.FILETIME lpExitTime, WinBase.FILETIME lpKernelTime, WinBase.FILETIME lpUserTime);

    /**
     * The Handle class is used as data structure to store the Thread handle of the Kernel32Library.
     */
    class Handle extends PointerType {
        /**
         * Constructor for the Handle class with the Pointer address.
         * @param address - the Pointer address.
         */
        public Handle(final Pointer address) {
            super(address);
        }

        /**
         * Default constructor of the Handle class.
         */
        public Handle() {
            super();
        }
    }
}
