package io.retit.opentelemetry.javaagent.extension.metrics;

import com.sun.management.OperatingSystemMXBean;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.retit.opentelemetry.javaagent.extension.Constants;
import io.retit.opentelemetry.javaagent.extension.TelemetryUtils;
import io.retit.opentelemetry.javaagent.extension.commons.NativeFacade;
import io.retit.opentelemetry.javaagent.extension.emissions.CloudCarbonFootprintData;
import io.retit.opentelemetry.javaagent.extension.emissions.embodied.EmbodiedEmissions;
import io.retit.opentelemetry.javaagent.extension.energy.MemoryEnergyData;
import io.retit.opentelemetry.javaagent.extension.energy.NetworkEnergyData;
import io.retit.opentelemetry.javaagent.extension.energy.StorageEnergyData;

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

/**
 * Handles the publishing of various emissions metrics to an OpenTelemetry meter.
 * This service is responsible for calculating and publishing emissions from storage, CPU, embedded components, and memory.
 */
public class MetricPublishingService {

    private static final Logger LOGGER = Logger.getLogger(MetricPublishingService.class.getName());

    private static MetricPublishingService instance = new MetricPublishingService();

    private LongCounter storageDemandMetricPublisher;
    private LongCounter memoryDemandMetricPublisher;
    //private LongCounter networkDemandMetricPublisher;
    private LongCounter cpuDemandMetricPublisher;

    /**
     * Returns the singleton instance of MetricPublishingService, creating it if necessary.
     *
     * @return The singleton instance of MetricPublishingService.
     */
    public static MetricPublishingService getInstance() {
        return instance;
    }

    /**
     * Private constructor for initializing the MetricPublishingService.
     * Initializes the meters for storage, CPU, embedded components, and memory emissions.
     */
    private MetricPublishingService() {

        Meter meter = GlobalOpenTelemetry.get().getMeter("opentelemetry-javaagent-extension");

        // CPU time of the whole process
        meter.counterBuilder(Constants.SPAN_ATTRIBUTE_PROCESS_CPU_TIME)
                .buildWithCallback(this::publishProcessCPUTime);

        // minimum power consumption of the CPU in Idle
        meter.gaugeBuilder("io.retit.emissions.cpu.power.min")
                .buildWithCallback(measurement ->
                        publishDoubleMeasurement(measurement, "Min CPU Power Consumption", CloudCarbonFootprintData.getConfigInstance().getCloudInstanceDetails().getCpuPowerConsumptionIdle())
                );

        // maximum power consumption of the CPU at 100% utilization
        meter.gaugeBuilder("io.retit.emissions.cpu.power.max")
                .buildWithCallback(measurement ->
                        publishDoubleMeasurement(measurement, "Max CPU Power Consumption", CloudCarbonFootprintData.getConfigInstance().getCloudInstanceDetails().getCpuPowerConsumption100Percent()));

        // embodied emissions per minute in mg
        meter.gaugeBuilder("io.retit.emissions.embodied.emissions.minute.mg")
                .buildWithCallback(measurement ->
                        publishDoubleMeasurement(measurement, "Embodied Emissions per Minute in mg", EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInMilliGramPerMinute()));

        // Memory energy consumption in kWh per GB per minute
        meter.gaugeBuilder("io.retit.emissions.memory.energy.gb.minute")
                .buildWithCallback(measurement ->
                        publishDoubleMeasurement(measurement, " Memory energy consumption in kWh per GB per minute", MemoryEnergyData.getInstance().getKwhPerGBMinute()));

        // Storage energy consumption in kWh per GB per minute
        meter.gaugeBuilder("io.retit.emissions.storage.energy.gb.minute")
                .buildWithCallback(measurement ->
                        publishDoubleMeasurement(measurement, "Storage energy consumption in kWh per GB per minute", StorageEnergyData.getInstance().getKwhPerGBMinute()));

        // Network energy consumption in kWh per GB per minute
        meter.gaugeBuilder("io.retit.emissions.network.energy.gb.minute")
                .buildWithCallback(measurement -> publishDoubleMeasurement(measurement, "Network energy consumption in kWh per GB per minute", NetworkEnergyData.getInstance().getKwhPerGBMinute()));

        // Power Usage Effectiveness (PUE) value of the datacenter
        meter.gaugeBuilder("io.retit.emissions.pue")
                .buildWithCallback(measurement -> publishDoubleMeasurement(measurement, "Power Usage Effectiveness (PUE) value of the datacenter", CloudCarbonFootprintData.getConfigInstance().getPueValue()));

        // Grid Emissions Factor (GEF)
        meter.gaugeBuilder("io.retit.emissions.gef")
                .buildWithCallback(measurement -> publishDoubleMeasurement(measurement, "Grid Emissions Factor (GEF) in the region", CloudCarbonFootprintData.getConfigInstance().getGridEmissionsFactor()));

        storageDemandMetricPublisher = meter.counterBuilder("io.retit.resource.demand.storage.bytes").setUnit("bytes")
                .setDescription("Storage demand of a transaction in bytes").build();

        memoryDemandMetricPublisher = meter.counterBuilder("io.retit.resource.demand.memory.bytes").setUnit("bytes")
                .setDescription("Memory demand of a transaction in bytes").build();

        // networkDemandMetricPublisher = meter.counterBuilder("io.retit.resource.demand.network.bytes").setUnit("bytes")
        //        .setDescription("Memory demand of a transaction in bytes").build();

        cpuDemandMetricPublisher = meter.counterBuilder("io.retit.resource.demand.cpu.ms").setUnit("ms")
                .setDescription("CPU demand of a transaction in ms").build();
    }

    private void publishProcessCPUTime(final ObservableLongMeasurement measurement) {
        LOGGER.info("Publishing CPU time");
        if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
            OperatingSystemMXBean sunOSBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            measurement.record(sunOSBean.getProcessCpuTime(), Attributes.of(AttributeKey.stringKey("io.retit.java.process.id"), String.valueOf(NativeFacade.getProcessId())));
        }
    }

    private void publishDoubleMeasurement(final ObservableDoubleMeasurement measurement, final String type, final double value) {
        LOGGER.info("Publishing " + type + " with value " + value);
        measurement.record(value, Attributes.of(AttributeKey.stringKey(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_CONFIGURATION_PROPERTY), CloudCarbonFootprintData.getConfigInstance().getCloudInstanceDetails().getCloudProvider().name(),
                AttributeKey.stringKey(Constants.RETIT_EMISSIONS_CLOUD_PROVIDER_INSTANCE_TYPE_CONFIGURATION_PROPERTY), CloudCarbonFootprintData.getConfigInstance().getCloudInstanceDetails().getInstanceType()));
    }

    /**
     * Publishes the resource demand vector for the current transaction.
     *
     * @param readWriteSpan      - the current span.
     * @param logCPUTime         - configuration whether CPU time should be published.
     * @param logHeapConsumption - configuration whether heap demand should be published.
     * @param logDiskDemand      - configuration whether disk demand should be published.
     */
    public void publishResourceDemandVectorOfTransaction(final ReadWriteSpan readWriteSpan, final boolean logCPUTime, final boolean logHeapConsumption, final boolean logDiskDemand) {
        if (!TelemetryUtils.isExternalDatabaseCall(readWriteSpan)) {
            Long startThread = readWriteSpan.getAttributes().get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_SPAN_START_THREAD));
            Long endThread = readWriteSpan.getAttributes().get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_SPAN_END_THREAD));

            if (startThread != null && startThread.equals(endThread)) {
                // add resource demands to resource demand vector
                publishCpuDemandMetricForTransaction(logCPUTime, readWriteSpan.getAttributes());
                publishMemoryDemandMetricForTransaction(logHeapConsumption, readWriteSpan.getAttributes());
                publishStorageDemandMetricForTransaction(logDiskDemand, readWriteSpan.getAttributes());
                // publishNetworkDemandMetricForTransaction()
            }
        }
    }

    private void publishStorageDemandMetricForTransaction(final boolean logDiskDemand, final Attributes spanAttributes) {
        if (logDiskDemand) {
            Long startDiskReadDemand = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_DISK_READ_DEMAND));
            Long endDiskReadDemand = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_DISK_READ_DEMAND));
            long totalDiskReadDemand = startDiskReadDemand != null && endDiskReadDemand != null ? endDiskReadDemand - startDiskReadDemand : 0;

            Long startDiskWriteDemand = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_DISK_WRITE_DEMAND));
            Long endDiskWriteDemand = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_DISK_WRITE_DEMAND));
            long totalDiskWriteDemand = startDiskWriteDemand != null && endDiskWriteDemand != null ? (endDiskWriteDemand - startDiskWriteDemand) : 0;

            long totalStorageDemand = totalDiskReadDemand + totalDiskWriteDemand;

            if (totalStorageDemand > 0) {
                storageDemandMetricPublisher.add(totalStorageDemand, spanAttributes);
            }

        }
    }

    private void publishMemoryDemandMetricForTransaction(final boolean logHeapDemand, final Attributes spanAttributes) {
        if (logHeapDemand) {
            Long startHeapByteAllocation = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_HEAP_BYTE_ALLOCATION));
            Long endHeapByteAllocation = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION));
            long totalHeapDemand = startHeapByteAllocation != null && endHeapByteAllocation != null ? endHeapByteAllocation - startHeapByteAllocation : 0;

            if (totalHeapDemand > 0) {
                memoryDemandMetricPublisher.add(totalHeapDemand, spanAttributes);
            }

        }
    }

    private void publishCpuDemandMetricForTransaction(final boolean logCpuDemand, final Attributes spanAttributes) {
        if (logCpuDemand) {
            Long startCpuTime = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_CPU_TIME));
            Long endCpuTime = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_CPU_TIME));
            long totalCpuTimeUsed = startCpuTime != null && endCpuTime != null ? endCpuTime - startCpuTime : 0;

            if (totalCpuTimeUsed > 0) {
                cpuDemandMetricPublisher.add(totalCpuTimeUsed, spanAttributes);
            }
        }
    }

    //private void publishNetworkDemandMetricForTransaction(final boolean logNetworkDemand, final Attributes spanAttributes) {
    //if (logNetworkDemand) {
    /*Long startDiskReadDemand = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_DISK_READ_DEMAND));
     Long endDiskReadDemand = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_DISK_READ_DEMAND));
     long totalDiskReadDemand = startDiskReadDemand != null && endDiskReadDemand != null ? endDiskReadDemand - startDiskReadDemand : 0;

     Long startDiskWriteDemand = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_START_DISK_WRITE_DEMAND));
     Long endDiskWriteDemand = spanAttributes.get(AttributeKey.longKey(Constants.SPAN_ATTRIBUTE_END_DISK_WRITE_DEMAND));
     long totalDiskWriteDemand = startDiskWriteDemand != null && endDiskWriteDemand != null ? (endDiskWriteDemand - startDiskWriteDemand) : 0;

     long totalStorageDemand = totalDiskReadDemand + totalDiskWriteDemand;

     storageDemandMetricPublisher.add(totalStorageDemand, spanAttributes);*/
    //}
    //}
}

