package io.retit.opentelemetry.javaagent.extension;

import com.sun.management.GarbageCollectionNotificationInfo;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.MemoryUsage;
import java.util.Map;

/**
 * A {@link NotificationListener} which triggers on garbage collection invocations.
 * This notification listener is registered by the {@link JavaAgentGCHandler} and then waits for garbage collection notifications.
 * Once such a notification arrives, handleGCNotification(Notification) is called.
 */
public class JavaAgentGCNotificationListener implements NotificationListener {

    private static final String END_OF_MINOR_GC = "end of minor GC";
    private static final String END_OF_MAJOR_GC = "end of major GC";

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        if (notification != null && GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION.equals(notification.getType())) {
            handleGCNotification(notification);
        }
    }

    private void handleGCNotification(final Notification notification) {
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

    private static String determineOperationName(final String garbageCollectionType) {
        if (END_OF_MINOR_GC.equals(garbageCollectionType)) {
            return Constants.JAVA_AGENT_GC_OPERATION_NAME_MINOR_FREE;
        } else if (END_OF_MAJOR_GC.equals(garbageCollectionType)) {
            return Constants.JAVA_AGENT_GC_OPERATION_NAME_MAJOR_FREE;
        }
        return "";
    }
}
