package com.sequenceiq.redbeams.flow.redbeams.provision.event.services;

import com.sequenceiq.freeipa.flow.stack.StackEvent;

public class InstallFreeIpaServicesSuccess extends StackEvent {
    public InstallFreeIpaServicesSuccess(Long stackId) {
        super(stackId);
    }
}
