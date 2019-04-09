package com.sequenceiq.cloudbreak.message;

public interface FlowMessageService {
    void fireEventAndLog(Long stackId, String message, NotificationEventType eventType);

    void fireEventAndLog(Long stackId, Msg msgCode, NotificationEventType eventType, Object... args);

    void fireInstanceGroupEventAndLog(Long stackId, Msg msgCode, NotificationEventType eventType, String instanceGroup, Object... args);

    String message(Msg msgCode, Object... args);

}
