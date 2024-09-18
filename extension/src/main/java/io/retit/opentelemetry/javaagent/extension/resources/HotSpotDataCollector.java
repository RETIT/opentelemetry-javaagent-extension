package io.retit.opentelemetry.javaagent.extension.resources;

/**
 * A JVM data collector used for both Oracle's HotSpot as well as OpenJDK JVMs.
 */
public class HotSpotDataCollector extends CommonResourceDemandDataCollector {

    @Override
    public long getCurrentThreadAllocatedBytes() {
        long bytes = -1;
        if (getThreadMXBean() instanceof com.sun.management.ThreadMXBean) {
            com.sun.management.ThreadMXBean sunThreadMXBean = (com.sun.management.ThreadMXBean) getThreadMXBean();
            // Returns an approximation of the total amount of memory, in bytes, allocated in heap memory for the thread of the specified ID.
            bytes = sunThreadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());
        }
        return bytes;
    }

    @Override
    public long[] getDiskBytesReadAndWritten() {
        throw new UnsupportedOperationException("Disk IO demand cannot read for HotSpot JVMs");
    }

    @Override
    public long[] getNetworkBytesReadAndWritten() {
        throw new UnsupportedOperationException("Network IO demand cannot read for HotSpot JVMs");
    }
}