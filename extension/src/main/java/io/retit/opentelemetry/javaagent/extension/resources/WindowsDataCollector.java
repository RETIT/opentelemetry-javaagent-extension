package io.retit.opentelemetry.javaagent.extension.resources;

import io.retit.opentelemetry.javaagent.extension.commons.NativeFacade;

/**
 * An {@link IResourceDemandDataCollector
 * IResourceDemandDataCollector} which retrieves resource demands on Windows
 * systems.
 * <p>
 * More specifically, this class implements {@link #getCurrentThreadCpuTime()}
 * in order to provide a more accurate CPU counter than the one available
 * through the JVM.
 *

 */
public class WindowsDataCollector extends CommonResourceDemandDataCollector {

    @Override
    public long getCurrentThreadCpuTime() {
        return NativeFacade.getCurrentThreadCpuTime();
    }

    @Override
    public long[] getDiskBytesReadAndWritten() {
        return new long[] {0, 0};
    }

}

