package com.sequenceiq.cloudbreak.structuredevent.event;

import java.util.List;

import com.sequenceiq.cloudbreak.message.NotificationEventType;


public interface CloudbreakEventService {

    void fireCloudbreakEvent(Long entityId, NotificationEventType eventType, String eventMessage);

    void fireLdapEvent(LdapDetails ldapDetails, NotificationEventType eventType, String eventMessage, boolean notificateUserOnly);

    void fireRdsEvent(RdsDetails rdsDetails, NotificationEventType eventType, String eventMessage, boolean notificateUserOnly);

    void fireCloudbreakInstanceGroupEvent(Long stackId, NotificationEventType eventType, String eventMessage, String instanceGroupName);

    List<StructuredNotificationEvent> cloudbreakEvents(Long workspaceId, Long since);

    List<StructuredNotificationEvent> cloudbreakEventsForStack(Long stackId);
}
