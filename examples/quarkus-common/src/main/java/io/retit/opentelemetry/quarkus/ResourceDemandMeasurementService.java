package io.retit.opentelemetry.quarkus;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.logging.Logger;

/**
 * This service can be used to measure the CPU and memory resource demands of a service.
 */
@ApplicationScoped
public class ResourceDemandMeasurementService {

    private static final Logger LOGGER = Logger.getLogger(ResourceDemandMeasurementService.class.getName());
    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

    static {
        if (THREAD_MX_BEAN.isThreadCpuTimeSupported()) {
            if (!THREAD_MX_BEAN.isThreadCpuTimeEnabled()) {
                THREAD_MX_BEAN.setThreadCpuTimeEnabled(true);
                LOGGER.info("Thread CPU time measurement enabled.");
            }
        } else {
            LOGGER.warning("Thread CPU time measurement is not supported by this JVM. CPU demand metrics will be 0.");
        }
    }

    @Inject
    private OpenTelemetryService otelService;

    /**
     * Returns the total CPU time used by the current thread since its creation.
     * Returns 0 if thread CPU time measurement is not supported or not enabled.
     *
     * @return - the total CPU time of the current thread in nanoseconds, or 0 if unavailable.
     */
    protected long getCurrentThreadCpuTime() {
        if (!THREAD_MX_BEAN.isThreadCpuTimeSupported() || !THREAD_MX_BEAN.isThreadCpuTimeEnabled()) {
            return 0L;
        }
        long cpuTime = THREAD_MX_BEAN.getCurrentThreadCpuTime();
        return cpuTime == -1 ? 0L : cpuTime;
    }

    /**
     * Returns the total heap allocation by the current thread since its creation.
     *
     * @return - the total heap allocation of the current thread in bytes.
     */
    protected long getCurrentThreadMemoryConsumption() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        if (mxBean instanceof com.sun.management.ThreadMXBean) {
            com.sun.management.ThreadMXBean sunThreadMXBean = (com.sun.management.ThreadMXBean) mxBean;
            if (sunThreadMXBean.isThreadAllocatedMemorySupported()) {
                return sunThreadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());
            } else {
                return 0l;
            }

        } else {
            return 0l;
        }

    }

    /**
     * Measures the CPU time and memory consumption of the current thread.
     *
     * @return a Measurement object containing the CPU time and memory consumption of the current thread.
     */
    public Measurement measure() {
        return new Measurement(getCurrentThreadCpuTime(), getCurrentThreadMemoryConsumption());
    }

    /**
     * Calculates the CPU and memory consumption of the current thread based on the startMeasurement
     * and publishes the results as OpenTelemetry metrics using the attributes HTTP method and apiCall name
     * provided as method parameters.
     *
     * @param startMeasurement - the measurement object collected at the beginning of a service operation.
     * @param httpMethod       - the HTTP method of the service operation that will be added as metric attribute.
     * @return - the attributes published along with the resource demand metrics.
     */
    public Attributes measureAndPublishMetrics(final Measurement startMeasurement, final String httpMethod) {
        Measurement endMeasurement = measure();

        Attributes attributes = Attributes.of(AttributeKey.stringKey("httpmethod"), httpMethod);

        otelService.publishCpuTimeMetric((endMeasurement.getCpuTime() - startMeasurement.getCpuTime()) / 1_000_000, attributes);
        otelService.publishMemoryDemandMetric((endMeasurement.getBytes() - startMeasurement.getBytes()) / 1_000, attributes);
        otelService.publishCallCountMetric(attributes);

        return attributes;
    }

    /**
     * Data structure to capture the resource measurements.
     */
    public static final class Measurement {
        long cpuTime;
        long bytes;

        public Measurement(final long cpuTime, final long bytes) {
            this.cpuTime = cpuTime;
            this.bytes = bytes;
        }

        public long getCpuTime() {
            return cpuTime;
        }

        public long getBytes() {
            return bytes;
        }
    }

}
