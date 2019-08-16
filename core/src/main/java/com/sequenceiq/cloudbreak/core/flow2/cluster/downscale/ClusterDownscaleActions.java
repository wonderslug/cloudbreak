package com.sequenceiq.cloudbreak.core.flow2.cluster.downscale;

import static com.sequenceiq.cloudbreak.core.flow2.cluster.downscale.ClusterDownscaleEvent.FAILURE_EVENT;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;

import com.sequenceiq.cloudbreak.cloud.event.Selectable;
import com.sequenceiq.cloudbreak.core.flow2.ContextKeys;
import com.sequenceiq.cloudbreak.core.flow2.cluster.AbstractClusterAction;
import com.sequenceiq.cloudbreak.core.flow2.cluster.ClusterViewContext;
import com.sequenceiq.cloudbreak.core.flow2.cluster.start.ClusterStartEvent;
import com.sequenceiq.cloudbreak.core.flow2.cluster.start.ClusterStartState;
import com.sequenceiq.cloudbreak.core.flow2.event.ClusterDownscaleTriggerEvent;
import com.sequenceiq.cloudbreak.core.flow2.stack.AbstractStackFailureAction;
import com.sequenceiq.cloudbreak.core.flow2.stack.StackFailureContext;
import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.StackFailureEvent;
import com.sequenceiq.cloudbreak.reactor.api.event.orchestration.RemoveHostsFailed;
import com.sequenceiq.cloudbreak.reactor.api.event.orchestration.RemoveHostsRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.orchestration.RemoveHostsSuccess;
import com.sequenceiq.cloudbreak.reactor.api.event.resource.CollectDownscaleCandidatesRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.resource.CollectDownscaleCandidatesResult;
import com.sequenceiq.cloudbreak.reactor.api.event.resource.DecommissionRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.resource.DecommissionResult;

@Configuration
public class ClusterDownscaleActions {
    @Inject
    private ClusterDownscaleService clusterDownscaleService;

    @Bean(name = "COLLECT_CANDIDATES_STATE")
    public Action<?, ?> collectCandidatesAction() {
        return new AbstractClusterAction<>(ClusterDownscaleTriggerEvent.class) {
            @Override
            protected void doExecute(ClusterViewContext context, ClusterDownscaleTriggerEvent payload, Map<Object, Object> variables) {
                clusterDownscaleService.clusterDownscaleStarted(context.getStackId(), payload.getHostGroupName(), payload.getAdjustment(),
                        payload.getPrivateIds(), payload.getDetails());
                CollectDownscaleCandidatesRequest request = new CollectDownscaleCandidatesRequest(context.getStackId(), payload.getHostGroupName(),
                        payload.getAdjustment(), payload.getPrivateIds(), payload.getDetails());
                sendEvent(context.getFlowId(), request.selector(), request);
            }
        };
    }

    @Bean(name = "DECOMMISSION_STATE")
    public Action<?, ?> decommissionAction() {
        return new AbstractClusterAction<>(CollectDownscaleCandidatesResult.class) {
            @Override
            protected void doExecute(ClusterViewContext context, CollectDownscaleCandidatesResult payload, Map<Object, Object> variables) {
                variables.put(ContextKeys.PRIVATE_IDS, payload.getPrivateIds());
                DecommissionRequest request = new DecommissionRequest(context.getStackId(), payload.getHostGroupName(), payload.getPrivateIds(),
                        payload.getRequest().getDetails());
                sendEvent(context.getFlowId(), request.selector(), request);
            }
        };
    }

    @Bean(name = "REMOVE_HOSTS_FROM_ORCHESTRATION_STATE")
    public Action<?, ?> removeHostsFromOrchestrationAction() {
        return new AbstractClusterAction<>(DecommissionResult.class) {
            @Override
            protected void doExecute(ClusterViewContext context, DecommissionResult payload, Map<Object, Object> variables) {
                RemoveHostsRequest request = new RemoveHostsRequest(context.getStackId(), payload.getHostGroupName(), payload.getHostNames());
                sendEvent(context.getFlowId(), request.selector(), request);
            }
        };
    }

    @Bean(name = "UPDATE_INSTANCE_METADATA_STATE")
    public Action<?, ?> updateInstanceMetadataAction() {
        return new AbstractClusterAction<>(RemoveHostsSuccess.class) {
            @Override
            protected void doExecute(ClusterViewContext context, RemoveHostsSuccess payload, Map<Object, Object> variables) {
                clusterDownscaleService.updateMetadata(context.getStackId(), payload.getHostNames(), payload.getHostGroupName());
                sendEvent(context);
            }

            @Override
            protected Selectable createRequest(ClusterViewContext context) {
                return new StackEvent(ClusterDownscaleEvent.FINALIZED_EVENT.event(), context.getStackId());
            }
        };
    }

    @Bean(name = "DECOMISSION_FAILED_STATE")
    public Action<?, ?> updateInstanceMetadataDecomissionFailedAction() {
        return new AbstractClusterAction<>(DecommissionResult.class) {
            @Override
            protected void doExecute(ClusterViewContext context, DecommissionResult payload, Map<Object, Object> variables) {
                if (payload.getErrorDetails() != null) {
                    clusterDownscaleService.updateMetadataStatus(payload);
                    sendEvent(context.getFlowId(), FAILURE_EVENT.event(), new StackFailureEvent(payload.getStackId(), payload.getErrorDetails()));
                }
            }
        };
    }

    @Bean(name = "REMOVE_HOSTS_FROM_ORCHESTRATION_FAILED_STATE")
    public Action<?, ?> updateInstanceMetadataOrchestrationFailedAction() {
        return new AbstractClusterAction<>(RemoveHostsFailed.class) {
            @Override
            protected void doExecute(ClusterViewContext context, RemoveHostsFailed payload, Map<Object, Object> variables) {
                clusterDownscaleService.updateMetadataStatus(payload);
                sendEvent(context.getFlowId(), FAILURE_EVENT.event(), new StackFailureEvent(payload.getStackId(), payload.getException()));
            }
        };
    }

    @Bean(name = "CLUSTER_DOWNSCALE_FAILED_STATE")
    public Action<?, ?> clusterDownscalescaleFailedAction() {
        return new AbstractStackFailureAction<ClusterStartState, ClusterStartEvent>() {
            @Override
            protected void doExecute(StackFailureContext context, StackFailureEvent payload, Map<Object, Object> variables) {
                clusterDownscaleService.handleClusterDownscaleFailure(context.getStackView().getId(), payload.getException());
                sendEvent(context);
            }

            @Override
            protected Selectable createRequest(StackFailureContext context) {
                return new StackEvent(ClusterDownscaleEvent.FAIL_HANDLED_EVENT.event(), context.getStackView().getId());
            }
        };
    }
}
