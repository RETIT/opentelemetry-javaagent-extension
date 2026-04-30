package io.retit.opentelemetry.javaagent.extension.common;

public class SpanDemand {
    public Long startCpuTime = null;
    public Long endCpuTime = null;
    public Long startSystemTime = null;
    public Long endSystemTime = null;
    public Long startHeapDemand = null;
    public Long endHeapDemand = null;
    public Long startDiskReadDemand = null;
    public Long endDiskReadDemand = null;
    public Long startDiskWriteDemand = null;
    public Long endDiskWriteDemand = null;
    public Long startNetworkReadDemand = null;
    public Long endNetworkReadDemand = null;
    public Long startNetworkWriteDemand = null;
    public Long endNetworkWriteDemand = null;
    public Long logSystemTime = null;
    public Long totalHeapSize = null;
    public Long startThreadId = null;
    public Long endThreadId = null;

    public static SpanDemand parseProperties(String logOutput) {
        SpanDemand spanDemand = new SpanDemand();
        String[] properties = extractPropertiesFromLogOutput(logOutput);
        for (String prop : properties) {
            String[] elems = prop.split("=");
            if (elems[0].contains("io.retit.endcputime")) {
                spanDemand.endCpuTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.enddiskreaddemand")) {
                spanDemand.endDiskReadDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.enddiskwritedemand")) {
                spanDemand.endDiskWriteDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.endnetworkreaddemand")) {
                spanDemand.endNetworkReadDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.endnetworkwritedemand")) {
                spanDemand.endNetworkWriteDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.endheapbyteallocation")) {
                spanDemand.endHeapDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.endsystemtime")) {
                spanDemand.endSystemTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startcputime")) {
                spanDemand.startCpuTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startdiskreaddemand")) {
                spanDemand.startDiskReadDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startdiskwritedemand")) {
                spanDemand.startDiskWriteDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startnetworkreaddemand")) {
                spanDemand.startNetworkReadDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startnetworkwritedemand")) {
                spanDemand.startNetworkWriteDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startheapbyteallocation")) {
                spanDemand.startHeapDemand = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startsystemtime")) {
                spanDemand.startSystemTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.logsystemtime")) {
                spanDemand.logSystemTime = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.totalheapsize")) {
                spanDemand.totalHeapSize = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.startthread")) {
                spanDemand.startThreadId = Long.valueOf(elems[1]);
            } else if (elems[0].contains("io.retit.endthread")) {
                spanDemand.endThreadId = Long.valueOf(elems[1]);
            }
        }
        return spanDemand;
    }

    private static String[] extractPropertiesFromLogOutput(String logOutput) {
        return logOutput.substring(logOutput.indexOf("={") + 1, logOutput.lastIndexOf("},")).split(",");
    }
}
