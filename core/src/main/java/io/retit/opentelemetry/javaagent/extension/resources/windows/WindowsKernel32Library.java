package io.retit.opentelemetry.javaagent.extension.resources.windows;

import com.sun.jna.IntegerType;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.LongByReference;
import io.retit.opentelemetry.javaagent.extension.resources.common.NativeFacade;
import io.retit.opentelemetry.javaagent.extension.resources.common.ThreadHandle;

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
public interface WindowsKernel32Library extends Library {
    /**
     * The instance provided by the JNA library to interact with the native kernel 32 library.
     */
    WindowsKernel32Library INSTANCE = Native.loadLibrary("kernel32", WindowsKernel32Library.class);

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
    ThreadHandle GetCurrentThread();

    /**
     * Retrieves the cycle time for the specified thread.
     * See https://learn.microsoft.com/en-us/windows/win32/api/realtimeapiset/nf-realtimeapiset-querythreadcycletime.
     *
     * @param threadHandle - A handle to the thread.
     * @param cycles       - Ths reference will contain the number of CPU clock cycles used by the thread (incl. Kernel and User mode).
     * @return - a boolean indicator if the call was successful, the return value is in the cycles reference.
     */
    boolean QueryThreadCycleTime(ThreadHandle threadHandle, LongByReference cycles);

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
    boolean GetThreadTimes(ThreadHandle threadHandle, FILETIME lpCreationTime, FILETIME lpExitTime, FILETIME lpKernelTime, FILETIME lpUserTime);

    /**
     * Simplified version of the FILETIME datastructure.
     * <p>
     * See https://github.com/java-native-access/jna/blob/5.16.0/contrib/platform/src/com/sun/jna/platform/win32/WinBase.java#L811
     */
    @Structure.FieldOrder({"dwLowDateTime", "dwHighDateTime"})
    class FILETIME extends Structure {
        public int dwLowDateTime;
        public int dwHighDateTime;

        /**
         * <p>Converts the two 32-bit unsigned integer parts of this filetime
         * into a 64-bit unsigned integer representing the number of
         * 100-nanosecond intervals since January 1, 1601 (UTC).</p>
         *
         * @return This filetime as a 64-bit unsigned integer number of
         * 100-nanosecond intervals since January 1, 1601 (UTC).
         */
        public DWORDLONG toDWordLong() {
            return new DWORDLONG((long) dwHighDateTime << 32 | dwLowDateTime & 0xffffffffL);
        }
    }

    /**
     * 64-bit unsigned integer.
     */
    class DWORDLONG extends IntegerType {

        /**
         * The Constant SIZE.
         */
        public static final int SIZE = 8;

        /**
         * Instantiates a new dwordlong.
         *
         * @param value the value
         */
        public DWORDLONG(long value) {
            super(SIZE, value, true);
        }
    }
}
