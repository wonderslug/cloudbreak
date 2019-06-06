package com.sequenceiq.redbeams.flow.redbeams.provision;

import com.sequenceiq.flow.core.FlowEvent;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.freeipa.flow.freeipa.provision.event.bootstrap.BootstrapMachinesFailed;
import com.sequenceiq.freeipa.flow.freeipa.provision.event.bootstrap.BootstrapMachinesSuccess;
import com.sequenceiq.freeipa.flow.freeipa.provision.event.hostmetadatasetup.HostMetadataSetupFailed;
import com.sequenceiq.freeipa.flow.freeipa.provision.event.hostmetadatasetup.HostMetadataSetupSuccess;
import com.sequenceiq.freeipa.flow.freeipa.provision.event.services.InstallFreeIpaServicesFailed;
import com.sequenceiq.freeipa.flow.freeipa.provision.event.services.InstallFreeIpaServicesSuccess;

public enum RedbeamsProvisionEvent implements FlowEvent {
    REDBEAMS_PROVISION_EVENT("REDBEAMS_PROVISION_EVENT"),
    ALLOCATE_DATABASE_FINISHED_EVENT
    BOOTSTRAP_MACHINES_FINISHED_EVENT(EventSelectorUtil.selector(BootstrapMachinesSuccess.class)),
    BOOTSTRAP_MACHINES_FAILED_EVENT(EventSelectorUtil.selector(BootstrapMachinesFailed.class)),
    HOST_METADATASETUP_FINISHED_EVENT(EventSelectorUtil.selector(HostMetadataSetupSuccess.class)),
    HOST_METADATASETUP_FAILED_EVENT(EventSelectorUtil.selector(HostMetadataSetupFailed.class)),
    REDBEAMS_PROVISION_FAILED_EVENT("REDBEAMS_PROVISION_FAILED_EVENT"),
    REDBEAMS_PROVISION_FINISHED_EVENT("REDBEAMS_PROVISION_FINISHED_EVENT"),
    REDBEAMS_PROVISION_FAILURE_HANDLED_EVENT("REDBEAMS_PROVISION_FAILURE_HANDLED_EVENT");

    private final String event;

    RedbeamsProvisionEvent(String event) {
        this.event = event;
    }

    @Override
    public String event() {
        return event;
    }
}
