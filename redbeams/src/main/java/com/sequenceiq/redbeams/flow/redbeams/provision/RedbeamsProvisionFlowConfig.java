package com.sequenceiq.redbeams.flow.redbeams.provision;

import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent.BOOTSTRAP_MACHINES_FAILED_EVENT;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent.BOOTSTRAP_MACHINES_FINISHED_EVENT;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent.FREEIPA_INSTALL_FAILED_EVENT;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent.FREEIPA_INSTALL_FINISHED_EVENT;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent.REDBEAMS_PROVISION_EVENT;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent.REDBEAMS_PROVISION_FAILED_EVENT;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent.REDBEAMS_PROVISION_FAILURE_HANDLED_EVENT;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent.REDBEAMS_PROVISION_FINISHED_EVENT;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent.HOST_METADATASETUP_FAILED_EVENT;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionEvent.HOST_METADATASETUP_FINISHED_EVENT;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionState.BOOTSTRAPPING_MACHINES_STATE;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionState.COLLECTING_HOST_METADATA_STATE;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionState.FINAL_STATE;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionState.FREEIPA_INSTALL_STATE;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionState.FREEIPA_PROVISION_FAILED_STATE;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionState.FREEIPA_PROVISION_FINISHED_STATE;
import static com.sequenceiq.redbeams.flow.redbeams.provision.RedbeamsProvisionState.INIT_STATE;

import com.sequenceiq.flow.core.config.AbstractFlowConfiguration;
import com.sequenceiq.flow.core.config.AbstractFlowConfiguration.Transition.Builder;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class RedbeamsProvisionFlowConfig extends AbstractFlowConfiguration<RedbeamsProvisionState, RedbeamsProvisionEvent> {

    private static final RedbeamsProvisionEvent[] FREEIPA_INIT_EVENTS = {REDBEAMS_PROVISION_EVENT};

    private static final FlowEdgeConfig<RedbeamsProvisionState, RedbeamsProvisionEvent> EDGE_CONFIG =
            new FlowEdgeConfig<>(INIT_STATE, FINAL_STATE, FREEIPA_PROVISION_FAILED_STATE, REDBEAMS_PROVISION_FAILURE_HANDLED_EVENT);

    private static final List<Transition<RedbeamsProvisionState, RedbeamsProvisionEvent>> TRANSITIONS =
            new Builder<RedbeamsProvisionState, RedbeamsProvisionEvent>().defaultFailureEvent(REDBEAMS_PROVISION_FAILED_EVENT)
            .from(INIT_STATE).to(BOOTSTRAPPING_MACHINES_STATE).event(REDBEAMS_PROVISION_EVENT).noFailureEvent()
            .from(BOOTSTRAPPING_MACHINES_STATE).to(COLLECTING_HOST_METADATA_STATE).event(BOOTSTRAP_MACHINES_FINISHED_EVENT)
                    .failureEvent(BOOTSTRAP_MACHINES_FAILED_EVENT)
            .from(COLLECTING_HOST_METADATA_STATE).to(FREEIPA_INSTALL_STATE).event(HOST_METADATASETUP_FINISHED_EVENT)
                    .failureEvent(HOST_METADATASETUP_FAILED_EVENT)
            .from(FREEIPA_INSTALL_STATE).to(FREEIPA_PROVISION_FINISHED_STATE).event(FREEIPA_INSTALL_FINISHED_EVENT).failureEvent(FREEIPA_INSTALL_FAILED_EVENT)
            .from(FREEIPA_PROVISION_FINISHED_STATE).to(FINAL_STATE).event(REDBEAMS_PROVISION_FINISHED_EVENT).defaultFailureEvent()
            .build();

    public RedbeamsProvisionFlowConfig() {
        super(RedbeamsProvisionState.class, RedbeamsProvisionEvent.class);
    }

    @Override
    protected List<Transition<RedbeamsProvisionState, RedbeamsProvisionEvent>> getTransitions() {
        return TRANSITIONS;
    }

    @Override
    protected FlowEdgeConfig<RedbeamsProvisionState, RedbeamsProvisionEvent> getEdgeConfig() {
        return EDGE_CONFIG;
    }

    @Override
    public RedbeamsProvisionEvent[] getEvents() {
        return RedbeamsProvisionEvent.values();
    }

    @Override
    public RedbeamsProvisionEvent[] getInitEvents() {
        return FREEIPA_INIT_EVENTS;
    }
}
