package com.sequenceiq.cloudbreak.cluster.service;

import static com.sequenceiq.cloudbreak.cloud.scheduler.PollGroup.CANCELLED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.cloudbreak.cloud.scheduler.PollGroup;
import com.sequenceiq.cloudbreak.cloud.store.InMemoryStateStore;
import com.sequenceiq.cloudbreak.polling.SimpleStatusCheckerService;

public abstract class ClusterBasedStatusCheckerService<T extends StackAware> extends SimpleStatusCheckerService<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterBasedStatusCheckerService.class);

    @Override
    public boolean exitPolling(T t) {
        try {
            Long stackId = t.getStack().getId();
            Long clusterId = t.getStack().getCluster().getId();
            PollGroup stackPollGroup = InMemoryStateStore.getStack(stackId);
            if (stackPollGroup == null || CANCELLED.equals(stackPollGroup)) {
                LOGGER.debug("Stack is getting terminated, polling is cancelled.");
                return true;
            }

            PollGroup clusterPollGroup = InMemoryStateStore.getCluster(clusterId);
            if (clusterPollGroup == null || CANCELLED.equals(clusterPollGroup)) {
                LOGGER.debug("Cluster is getting terminated, polling is cancelled.");
                return true;
            }

            return false;
        } catch (Exception ex) {
            LOGGER.info("Error occurred when check status checker exit criteria: ", ex);
            return true;
        }
    }
}
