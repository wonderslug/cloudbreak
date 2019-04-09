package com.sequenceiq.notification;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.message.NotificationEventType;
import com.sequenceiq.cloudbreak.structuredevent.event.responses.CloudbreakV4Event;

@Component
public class NotificationAssemblingService<T> {
    public Notification<T> createNotification(T notification) {
        return new Notification(notification);
    }

    public static CloudbreakV4Event cloudbreakEvent(Object payload, NotificationEventType eventType, String resource) {
        CloudbreakV4Event event = new CloudbreakV4Event();
        event.setEventType(eventType);
        event.setTimestamp(System.currentTimeMillis());
        event.setResourceName(resource);
        event.setPayload(payload);
        return event;
    }
}
