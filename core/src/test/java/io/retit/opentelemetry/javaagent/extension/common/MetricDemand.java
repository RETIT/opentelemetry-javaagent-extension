package io.retit.opentelemetry.javaagent.extension.common;

import java.util.ArrayList;
import java.util.List;

import static io.retit.opentelemetry.javaagent.extension.common.ContainerLogMetricAndSpanExtractingTest.METRIC_NAMES;

public class MetricDemand {
    public String metricName;
    public Double metricValue;

    public static List<MetricDemand> extractMetricValuesFromLog(String logMessage) {

        List<MetricDemand> demands = new ArrayList<>();
        String valueAttributeInLog = "value=";
        String sampleApplicationName = "io.retit.opentelemetry.SampleApplication";

        String emissionMetricNotTransactionRelated = "io.retit.emissions";

        for (String key : METRIC_NAMES) {
            if (logMessage.contains(key)) {
                String dataForCurrentMetric = logMessage.substring(logMessage.indexOf(key) + 1);

                if (!key.startsWith(emissionMetricNotTransactionRelated) && dataForCurrentMetric.contains(sampleApplicationName)) {
                    dataForCurrentMetric = dataForCurrentMetric.substring(dataForCurrentMetric.indexOf(sampleApplicationName));
                }

                if (dataForCurrentMetric.indexOf(valueAttributeInLog) != -1) {

                    MetricDemand metricDemand = new MetricDemand();
                    int valueIndex = dataForCurrentMetric.indexOf(valueAttributeInLog);

                    String valueString = dataForCurrentMetric.substring(valueIndex + valueAttributeInLog.length(), dataForCurrentMetric.indexOf(",", valueIndex));
                    double value = Double.parseDouble(valueString);

                    metricDemand.metricName = key;
                    metricDemand.metricValue = value;
                    demands.add(metricDemand);
                }

            }

        }
        return demands;
    }
}
