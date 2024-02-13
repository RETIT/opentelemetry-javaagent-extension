package io.retit.opentelemetry.javaagent.extension.resources;

public class IBMDataCollector extends CommonResourceDemandDataCollector {
    @Override
    public long getCurrentThreadAllocatedBytes() {
        throw new UnsupportedOperationException("Disk IO demand cannot read for IBM JVMs");
    }

    @Override
    public long[] getDiskBytesReadAndWritten() {
        throw new UnsupportedOperationException("Disk IO demand cannot read for IBM JVMs");
    }

}
