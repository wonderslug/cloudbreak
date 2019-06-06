package com.sequenceiq.redbeams.flow.redbeams.provision.event.services;

import com.sequenceiq.freeipa.flow.stack.StackFailureEvent;

public class InstallFreeIpaServicesFailed extends StackFailureEvent {
    public InstallFreeIpaServicesFailed(Long stackId, Exception exception) {
        super(stackId, exception);
    }
}
