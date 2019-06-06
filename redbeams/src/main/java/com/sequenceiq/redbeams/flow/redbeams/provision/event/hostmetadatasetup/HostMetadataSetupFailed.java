package com.sequenceiq.redbeams.flow.redbeams.provision.event.hostmetadatasetup;

import com.sequenceiq.freeipa.flow.stack.StackFailureEvent;

public class HostMetadataSetupFailed extends StackFailureEvent {
    public HostMetadataSetupFailed(Long stackId, Exception exception) {
        super(stackId, exception);
    }
}
