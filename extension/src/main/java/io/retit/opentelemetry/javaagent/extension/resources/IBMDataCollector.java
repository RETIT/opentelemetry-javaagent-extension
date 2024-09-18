package io.retit.opentelemetry.javaagent.extension.resources;

/**
 * Resource demand collector for IBM JVMs.
 */
public class IBMDataCollector extends CommonResourceDemandDataCollector {
    @Override
    public long getCurrentThreadAllocatedBytes() {
        throw new UnsupportedOperationException("Disk IO demand cannot read for IBM JVMs");
    }

    @Override
    public long[] getDiskBytesReadAndWritten() {
        throw new UnsupportedOperationException("Disk IO demand cannot read for IBM JVMs");
    }

    @Override
    public long[] getNetworkBytesReadAndWritten() {
        throw new UnsupportedOperationException("Network IO demand cannot read for IBM JVMs");
    }

}
