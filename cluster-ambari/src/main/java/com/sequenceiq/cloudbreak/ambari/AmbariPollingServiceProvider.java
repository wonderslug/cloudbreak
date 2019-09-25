package com.sequenceiq.cloudbreak.ambari;

import static com.sequenceiq.cloudbreak.ambari.flow.AmbariOperationService.AMBARI_POLLING_INTERVAL;
import static com.sequenceiq.cloudbreak.ambari.flow.AmbariOperationService.MAX_ATTEMPTS_FOR_AMBARI_SERVER_STARTUP;
import static com.sequenceiq.cloudbreak.ambari.flow.AmbariOperationService.MAX_ATTEMPTS_FOR_HOSTS;
import static com.sequenceiq.cloudbreak.ambari.flow.AmbariOperationService.MAX_FAILURE_COUNT;

import java.util.Arrays;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.ambari.client.AmbariClient;
import com.sequenceiq.cloudbreak.ambari.flow.AmbariClientPollerObject;
import com.sequenceiq.cloudbreak.ambari.flow.AmbariComponenstJoinStatusCheckerService;
import com.sequenceiq.cloudbreak.ambari.flow.AmbariHealthCheckerService;
import com.sequenceiq.cloudbreak.ambari.flow.AmbariHostsCheckerContext;
import com.sequenceiq.cloudbreak.ambari.flow.AmbariHostsJoinStatusCheckerService;
import com.sequenceiq.cloudbreak.ambari.flow.AmbariHostsStatusCheckerService;
import com.sequenceiq.cloudbreak.ambari.flow.AmbariStartupListenerService;
import com.sequenceiq.cloudbreak.ambari.flow.AmbariStartupPollerObject;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.cluster.host.HostMetadata;
import com.sequenceiq.cloudbreak.polling.PollingResult;
import com.sequenceiq.cloudbreak.polling.PollingService;

@Service
public class AmbariPollingServiceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmbariPollingServiceProvider.class);

    @Inject
    private PollingService<AmbariStartupPollerObject> ambariStartupPollerObjectPollingService;

    @Inject
    private PollingService<AmbariHostsCheckerContext> hostsPollingService;

    @Inject
    private PollingService<AmbariHostsCheckerContext> ambariHostJoin;

    @Inject
    private PollingService<AmbariClientPollerObject> ambariHealthChecker;

    @Inject
    private AmbariHostsStatusCheckerService ambariHostsStatusCheckerTask;

    @Inject
    private AmbariStartupListenerService ambariStartupListenerTask;

    @Inject
    private AmbariHealthCheckerService ambariHealthCheckerTask;

    @Inject
    private AmbariHostsJoinStatusCheckerService ambariHostsJoinStatusCheckerTask;

    @Inject
    private AmbariComponenstJoinStatusCheckerService ambariComponenstJoinStatusCheckerTask;

    public PollingResult ambariStartupPollerObjectPollingService(Stack stack, AmbariClient defaultAmbariClient, AmbariClient cloudbreakAmbariClient) {
        AmbariStartupPollerObject ambariStartupPollerObject = new AmbariStartupPollerObject(
                stack,
                stack.getClusterManagerIp(),
                Arrays.asList(defaultAmbariClient, cloudbreakAmbariClient));
        return ambariStartupPollerObjectPollingService
                .pollWithTimeoutSingleFailure(
                        ambariStartupListenerTask,
                        ambariStartupPollerObject,
                        AMBARI_POLLING_INTERVAL,
                        MAX_ATTEMPTS_FOR_AMBARI_SERVER_STARTUP);
    }

    public PollingResult hostsPollingService(Stack stack, AmbariClient ambariClient, Set<HostMetadata> hostsInCluster) {
        LOGGER.info("Waiting for hosts to connect.[Ambari server address: {}]", stack.getClusterManagerIp());
        return hostsPollingService.pollWithTimeoutSingleFailure(
                ambariHostsStatusCheckerTask,
                new AmbariHostsCheckerContext(stack, ambariClient, hostsInCluster, hostsInCluster.size()),
                AMBARI_POLLING_INTERVAL,
                MAX_ATTEMPTS_FOR_HOSTS);
    }

    public PollingResult ambariHostJoin(Stack stack, AmbariClient ambariClient, Set<HostMetadata> hostsInCluster) {
        AmbariHostsCheckerContext ambariHostsCheckerContext =
                new AmbariHostsCheckerContext(stack, ambariClient, hostsInCluster, stack.getFullNodeCount());
        return ambariHostJoin.pollWithTimeout(
                ambariHostsJoinStatusCheckerTask,
                ambariHostsCheckerContext,
                AMBARI_POLLING_INTERVAL,
                MAX_ATTEMPTS_FOR_HOSTS,
                MAX_FAILURE_COUNT).getLeft();
    }

    public PollingResult ambariComponentJoin(Stack stack, AmbariClient ambariClient, Set<HostMetadata> hostsInCluster) {
        AmbariHostsCheckerContext ambariHostsCheckerContext =
                new AmbariHostsCheckerContext(stack, ambariClient, hostsInCluster, stack.getFullNodeCount());
        return ambariHostJoin.pollWithTimeout(
                ambariComponenstJoinStatusCheckerTask,
                ambariHostsCheckerContext,
                AMBARI_POLLING_INTERVAL,
                MAX_ATTEMPTS_FOR_HOSTS,
                MAX_FAILURE_COUNT).getLeft();
    }

    public PollingResult ambariHealthChecker(Stack stack, AmbariClient ambariClient) {
        LOGGER.info("Checking if Ambari Server is available.");
        return ambariHealthChecker.pollWithTimeout(
                ambariHealthCheckerTask,
                new AmbariClientPollerObject(stack, ambariClient),
                AMBARI_POLLING_INTERVAL,
                MAX_ATTEMPTS_FOR_HOSTS,
                MAX_FAILURE_COUNT).getLeft();
    }

    public boolean isAmbariAvailable(Stack stack, AmbariClient ambariClient) {
        boolean result = false;
        if (stack.getCluster() != null) {
            AmbariClientPollerObject ambariClientPollerObject = new AmbariClientPollerObject(stack, ambariClient);
            try {
                result = ambariHealthCheckerTask.checkStatus(ambariClientPollerObject);
            } catch (Exception ignored) {
                result = false;
            }
        }
        return result;
    }
}
