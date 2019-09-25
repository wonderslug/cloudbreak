package com.sequenceiq.cloudbreak.core.bootstrap.service.host;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.core.bootstrap.service.host.context.HostOrchestratorClusterContext;
import com.sequenceiq.cloudbreak.service.StackBasedStatusCheckerService;

@Component
public class HostClusterAvailabilityCheckerService extends StackBasedStatusCheckerService<HostOrchestratorClusterContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostClusterAvailabilityCheckerService.class);

    @Override
    public boolean checkStatus(HostOrchestratorClusterContext context) {
        List<String> missingNodes = context.getHostOrchestrator().getMissingNodes(context.getGatewayConfig(), context.getNodes());
        LOGGER.debug("Missing nodes from orchestrator cluster: {}", missingNodes);
        return missingNodes.isEmpty();
    }

    @Override
    public void handleTimeout(HostOrchestratorClusterContext t) {
    }

    @Override
    public String successMessage(HostOrchestratorClusterContext t) {
        return "Host orchestration API is available and the agents are registered.";
    }
}
