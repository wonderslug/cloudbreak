package com.sequenceiq.redbeams.flow.redbeams.provision.event.bootstrap;

import com.sequenceiq.freeipa.flow.stack.StackEvent;

public class BootstrapMachinesSuccess extends StackEvent {
    public BootstrapMachinesSuccess(Long stackId) {
        super(stackId);
    }
}
