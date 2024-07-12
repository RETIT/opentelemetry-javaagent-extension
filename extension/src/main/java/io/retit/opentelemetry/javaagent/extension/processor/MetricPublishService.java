package io.retit.opentelemetry.javaagent.extension.processor;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.retit.opentelemetry.javaagent.extension.config.EnvVariables;
import io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu.CpuEmissions;
import io.retit.opentelemetry.javaagent.extension.emissionCalculations.cpu.EmbodiedEmissions;
import io.retit.opentelemetry.javaagent.extension.emissionCalculations.storage.StorageEmissions;

public class MetricPublishService {

    private static MetricPublishService instance;
    private final LongCounter serviceCallCounter;
    private final LongHistogram storageEmissionMeter;

    private MetricPublishService() {
        Meter meter = GlobalOpenTelemetry.get().getMeter("instrumentation-library-name");
        serviceCallCounter = meter.counterBuilder("testservice_call_count")
                .setDescription("number of times the test service has been called")
                .setUnit("1")
                .build();

        storageEmissionMeter = meter.histogramBuilder("storage_emissions")
                .setDescription("total emissions from storage")
                .setUnit("kgCO2e")
                .ofLongs().build();
    }

    public static synchronized MetricPublishService getInstance() {
        if (instance == null) {
            instance = new MetricPublishService();
        }
        return instance;
    }

    public void incrementServiceCallCounter(Attributes attributes) {
        serviceCallCounter.add(1, attributes);
    }

    public void publishStorageEmissions(EnvVariables envVariables, double totalStorageDemand, Attributes attributes) {
        double totalEmissions = StorageEmissions.calculateStorageEmissions(envVariables.getStorageType(), envVariables.getRegion(), totalStorageDemand);
        storageEmissionMeter.record((long) totalEmissions, attributes);
    }

    public void publishCpuEmissions(EnvVariables envVariables, double totalCpuDemand, Attributes attributes) {
        double totalEmissions = CpuEmissions.getInstance().calculateCpuEmissions(envVariables.getInstance(), totalCpuDemand);
        System.out.println("total cpu emissions: " + totalEmissions);
    }

    public void publishEmbeddedEmissions(EnvVariables envVariables, long totalCPUTimeUsedInHours, Attributes attributes) {
        //ignoring serverless software
        if (envVariables.getInstance() != null) {
            double totalEmbodiedEmissions = EmbodiedEmissions.getInstance().calculateEmbodiedEmissionsInGramm(envVariables.getInstance(), totalCPUTimeUsedInHours);
            System.out.println("total embodied emissions: " + totalEmbodiedEmissions);
        }
    }
}

    /*public void publishMetrics(Attributes attributes) {

        // Gets or creates a named meter instance
        Meter meter = GlobalOpenTelemetry.get().meterBuilder("instrumentation-library-name")
                .setInstrumentationVersion("1.0.0")
                .build();

        // Build counter e.g. LongCounter
        LongCounter counter = meter
                .counterBuilder("testservice_call_count")
                .setDescription("number of times the test service has been called")
                .setUnit("1")
                .build();

        // Record data
        counter.add(1, attributes);
    }*/

