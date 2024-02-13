package io.retit.opentelemetry.javaagent.extension;

import com.sun.management.GarbageCollectionNotificationInfo;
import java.util.logging.Logger;
import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * A {@link NotificationListener} which triggers on garbage collection invocations.
 * <p>
 * This notification listener is registered by the {@link JavaAgentGCHandler} and then waits for garbage collection notifications.
 * Once such a notification arrives, {@link JavaAgentGCHandler#handleGCNotification(Notification)} is called.
 */
public class JavaAgentGCNotificationListener implements NotificationListener {

    private static final Logger LOGGER = Logger.getLogger(JavaAgentGCNotificationListener.class.getName());

    @Override
    public void handleNotification(Notification notification, Object handback) {
        if (notification != null && notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
            try {
                JavaAgentGCHandler.handleGCNotification(notification);
            } catch (RuntimeException ex) {
                LOGGER.severe("Java Agent could not handle the gc notification. " + ex.getMessage());
            }
        }
    }
}
