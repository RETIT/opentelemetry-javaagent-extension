package io.retit.opentelemetry.javaagent.extension.resources;

import javax.management.ListenerNotFoundException;
import javax.management.NotificationEmitter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to register the {@link JavaAgentGCNotificationListener}.
 */
public class JavaAgentGCHandler {

    private static final Logger LOGGER = Logger.getLogger(JavaAgentGCHandler.class.getName());

    private static final JavaAgentGCNotificationListener JAVA_AGENT_GC_NOTIFICATION_LISTENER = new JavaAgentGCNotificationListener();

    /**
     * Registers the {@link JavaAgentGCNotificationListener} as lister to the {@link GarbageCollectorMXBean}.
     */
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
}
