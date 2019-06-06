package com.sequenceiq.redbeams.flow.redbeams.provision.event.hostmetadatasetup;

import com.sequenceiq.freeipa.flow.stack.StackEvent;

public class HostMetadataSetupSuccess extends StackEvent {
    public HostMetadataSetupSuccess(Long stackId) {
        super(stackId);
    }
}
