package io.retit.opentelemetry.javaagent.extension.resources;

/**
 * An {@link IResourceDemandDataCollector
 * IResourceDemandDataCollector} which retrieves resource demands on Windows
 * systems.
 * More specifically, this class implements {@link #getCurrentThreadCpuTime()}
 * in order to provide a more accurate CPU counter than the one available
 * through the JVM.
 */
public class WindowsDataCollector extends CommonResourceDemandDataCollector {

    @Override
    public long[] getDiskBytesReadAndWritten() {
        return new long[]{0, 0};
    }

}

