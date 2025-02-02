package io.retit.opentelemetry.quarkus;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * This service can be used to measure the CPU and memory resource demands of a service.
 */
@ApplicationScoped
public class ResourceDemandMeasurementService {

    @Inject
    private OpenTelemetryService otelService;

    /**
     * Data structure to capture the resource measurements.
     */
    public static final class Measurement {
        long cpuTime;
        long bytes;

        public Measurement(long cpuTime, long bytes) {
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

    /**
     * Returns the total CPU time used by the current thread since its creation.
     *
     * @return - the total CPU time of the current thread in nanoseconds.
     */
    protected long getCurrentThreadCpuTime() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        return mxBean.getCurrentThreadCpuTime();
    }

    /**
     * Returns the total heap allocation by the current thread since its creation.
     *
     * @return - the total heap allocation of the current thread in bytes.
     */
    protected long getCurrentThreadMemoryConsumption() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        com.sun.management.ThreadMXBean sunThreadMXBean = (com.sun.management.ThreadMXBean) mxBean;
        return sunThreadMXBean.getThreadAllocatedBytes(Thread.currentThread().getId());
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
    public Attributes measureAndPublishMetrics(Measurement startMeasurement, String httpMethod) {
        Measurement endMeasurement = measure();

        Attributes attributes = Attributes.of(AttributeKey.stringKey("httpmethod"), httpMethod);

        otelService.publishCpuTimeMetric((endMeasurement.getCpuTime() - startMeasurement.getCpuTime()) / 1_000_000, attributes);
        otelService.publishMemoryDemandMetric((endMeasurement.getBytes() - startMeasurement.getBytes()) / 1_000, attributes);
        otelService.publishCallCountMetric(attributes);

        return attributes;
    }

}
