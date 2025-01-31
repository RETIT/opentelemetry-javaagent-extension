package io.retit.opentelemetry.javaagent.extension.resources.common;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

/**
 * This interface provides access to the clock_gettime method of the time.h library.
 * See https://man7.org/linux/man-pages/man2/clock_gettime.2.html
 * This call can be used in Linux since 2.6.12 and for MacOS versions higher than 10.
 */
@SuppressWarnings("PMD")
public interface CLibrary extends Library {
    /**
     * This is a clock that measures CPU time consumed by this
     * thread.
     */
    int CLOCK_THREAD_CPUTIME_ID = 3;

    /**
     * Retrievs the time specified by the clock id.
     *
     * @param clockid -the clock for which the time needs to be fetched
     * @param tp      - pointer to the TimeSpec containing the measurements.
     * @return - return 0 for success, -1 on error
     */
    int clock_gettime(int clockid, TimeSpec tp);

    @Structure.FieldOrder({"tv_sec", "tv_nsec"})
    class TimeSpec extends Structure {
        public NativeLong tv_sec; // seconds
        public int tv_nsec; // nanoseconds
    }
}
