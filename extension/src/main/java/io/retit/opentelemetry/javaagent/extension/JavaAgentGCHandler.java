package io.retit.opentelemetry.javaagent.extension;

import com.sun.management.GarbageCollectionNotificationInfo;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.openmbean.CompositeData;

public class JavaAgentGCHandler {

    private static final Logger LOGGER = Logger.getLogger(JavaAgentGCHandler.class.getName());
    private static final String END_OF_MINOR_GC = "end of minor GC";
    private static final String END_OF_MAJOR_GC = "end of major GC";
    private static final JavaAgentGCNotificationListener JAVA_AGENT_GC_NOTIFICATION_LISTENER = new JavaAgentGCNotificationListener();

    public static void addJavaAgentGCListener() {
        for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (garbageCollectorMXBean instanceof NotificationEmitter) {
                NotificationEmitter notificationEmitter = (NotificationEmitter) garbageCollectorMXBean;
                try {
                    notificationEmitter.removeNotificationListener(JAVA_AGENT_GC_NOTIFICATION_LISTENER);
                } catch (ListenerNotFoundException ex) {
                    LOGGER.log(Level.FINE, "Error removing listener", ex);
                }
                notificationEmitter.addNotificationListener(JAVA_AGENT_GC_NOTIFICATION_LISTENER, null, null);
                LOGGER.log(Level.INFO, "Added JavaAgent GC Listener to {0}", garbageCollectorMXBean.getObjectName());
            } else {
                LOGGER.log(Level.WARNING,
                    "Could not register RETIT APM GC Lister as GC MXBean does not implement NotificationEmitter: {0}",
                    garbageCollectorMXBean.getObjectName());
            }
        }
    }

    public static synchronized void handleGCNotification(Notification notification) {
        GarbageCollectionNotificationInfo garbageCollectionInfo = GarbageCollectionNotificationInfo
            .from((CompositeData) notification.getUserData());

        final String garbageCollectionType = garbageCollectionInfo.getGcAction();
        final String operationName = determineOperationName(garbageCollectionType);
        final long duration = garbageCollectionInfo.getGcInfo().getDuration();

        // Get the information about each memory space
        Map<String, MemoryUsage> memorySpaceBefore = garbageCollectionInfo.getGcInfo().getMemoryUsageBeforeGc();
        Map<String, MemoryUsage> memory = garbageCollectionInfo.getGcInfo().getMemoryUsageAfterGc();

        long totalMemBeforeGC = 0;
        long totalMemAfterGC = 0;
        long memCommitted = 0;
        for (Map.Entry<String, MemoryUsage> entry : memory.entrySet()) {
            String name = entry.getKey();
            MemoryUsage memdetail = entry.getValue();
            MemoryUsage before = memorySpaceBefore.get(name);

            totalMemBeforeGC += before.getUsed();
            totalMemAfterGC += memdetail.getUsed();
            memCommitted += memdetail.getCommitted();
        }

        Span span =
            GlobalOpenTelemetry.getTracer(Constants.JAVA_AGENT_INSTRUMENTATION_NAME_GC_LISTENER).spanBuilder(operationName).setNoParent().startSpan();
        span.setAttribute(Constants.SPAN_ATTRIBUTE_START_SYSTEM_TIME, 0);
        span.setAttribute(Constants.SPAN_ATTRIBUTE_END_SYSTEM_TIME, duration);
        span.setAttribute(Constants.SPAN_ATTRIBUTE_START_HEAP_BYTE_ALLOCATION, totalMemBeforeGC);
        span.setAttribute(Constants.SPAN_ATTRIBUTE_END_HEAP_BYTE_ALLOCATION, totalMemAfterGC);
        span.setAttribute(Constants.SPAN_ATTRIBUTE_TOTAL_HEAP_SIZE, memCommitted);
        span.end();
    }

    private static String determineOperationName(String garbageCollectionType) {
        if (END_OF_MINOR_GC.equals(garbageCollectionType)) {
            return Constants.JAVA_AGENT_GC_OPERATION_NAME_MINOR_FREE;
        } else if (END_OF_MAJOR_GC.equals(garbageCollectionType)) {
            return Constants.JAVA_AGENT_GC_OPERATION_NAME_MAJOR_FREE;
        }
        return "";
    }
}
