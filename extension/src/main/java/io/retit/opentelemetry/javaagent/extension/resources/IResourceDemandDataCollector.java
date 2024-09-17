package io.retit.opentelemetry.javaagent.extension.resources;

/**
 * Interface which defines methods for retrieving resource demands.
 */
public interface IResourceDemandDataCollector {

    /**
     * get the number of currently allocated bytes for this thread.
     *
     * @return the number of bytes allocated
     */
    long getCurrentThreadAllocatedBytes();

    /**
     * get the current thread's CPU time.
     *
     * @return the current CPU time
     */
    long getCurrentThreadCpuTime();

    /**
     * get the amount of bytes read and written to the disk by current thread.
     *
     * @return an array were the first element depicts the amount of bytes read to the disk and
     * the second element depicts the amount of bytes written
     */
    long[] getDiskBytesReadAndWritten();
}
