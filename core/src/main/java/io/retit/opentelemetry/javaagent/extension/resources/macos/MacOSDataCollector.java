package io.retit.opentelemetry.javaagent.extension.resources.macos;

import io.retit.opentelemetry.javaagent.extension.resources.common.IResourceDemandDataCollector;
import io.retit.opentelemetry.javaagent.extension.resources.common.NativeFacade;
import io.retit.opentelemetry.javaagent.extension.resources.common.CommonResourceDemandDataCollector;

/**
 * An {@link IResourceDemandDataCollector
 * IResourceDemandDataCollector} which retrieves resource demands on Mac
 * systems.
 */
public class MacOSDataCollector extends CommonResourceDemandDataCollector {
    @Override
    protected long getPlatformSpecificThreadCpuTime() {
        return NativeFacade.getCurrentThreadCpuTime();
    }
}
