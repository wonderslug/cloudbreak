package com.sequenceiq.notification;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.message.CloudbreakMessagesService;
import com.sequenceiq.cloudbreak.message.NotificationEventType;
import com.sequenceiq.cloudbreak.structuredevent.event.responses.CloudbreakV4Event;
import com.sequenceiq.cloudbreak.workspace.resource.WorkspaceResource;

public abstract class NotificationController {

    @Inject
    private CloudbreakMessagesService messagesService;

    @Inject
    private NotificationSender notificationSender;

    @Inject
    private ThreadBasedUserCrnProvider threadBasedUserCrnProvider;

    protected final void notify(Object payload, NotificationEventType eventType, WorkspaceResource resource) {
        notify(payload, eventType, resource.getShortName(), Collections.emptySet());
    }
    protected final void notify(Object payload, NotificationEventType eventType, String resource) {
        notify(payload, eventType, resource, Collections.emptySet());
    }

    protected final void notify(Object payload, NotificationEventType eventType, String resource, Collection<?> messageArgs) {
        CloudbreakV4Event notification = NotificationAssemblingService.cloudbreakEvent(payload, eventType, resource);
        notification.setUser(threadBasedUserCrnProvider.getUserCrn());
        notification.setMessage(messagesService.getMessage(resource, eventType, messageArgs));
        notificationSender.send(new Notification<>(notification));
    }
}