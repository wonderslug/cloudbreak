package com.sequenceiq.cloudbreak.reactor.handler.cluster;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cluster.api.ClusterApi;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.DisableKerberosRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.DisableKerberosResult;
import com.sequenceiq.cloudbreak.service.cluster.ClusterApiConnectors;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.flow.reactor.api.handler.EventHandler;

import reactor.bus.Event;
import reactor.bus.EventBus;

@Component
public class DisableKerberosHandler implements EventHandler<DisableKerberosRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisableKerberosHandler.class);

    @Inject
    private EventBus eventBus;

    @Inject
    private ClusterApiConnectors clusterApiConnectors;

    @Inject
    private StackService stackService;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(DisableKerberosRequest.class);
    }

    @Override
    public void accept(Event<DisableKerberosRequest> event) {
        DisableKerberosResult result;
        try {
            LOGGER.info("Received DisableKerberosRequest event: {}", event.getData());
            ClusterApi clusterApi = clusterApiConnectors.getConnector(stackService.getByIdWithListsInTransaction(event.getData().getResourceId()));
            clusterApi.clusterSecurityService().disableSecurity();
            LOGGER.info("Finished disabling Security");
            result = new DisableKerberosResult(event.getData());
        } catch (Exception e) {
            LOGGER.warn("An error has occured during disabling security", e);
            result = new DisableKerberosResult(e.getMessage(), e, event.getData());
        }
        LOGGER.info("Sending out DisableKerberosResult: {}", result);
        eventBus.notify(result.selector(), new Event<>(event.getHeaders(), result));
        LOGGER.info("DisableKerberosResult has been sent");
    }
}
