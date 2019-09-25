package com.sequenceiq.cloudbreak.cm.polling;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cloudera.api.swagger.client.ApiClient;
import com.sequenceiq.cloudbreak.cm.polling.service.AbstractClouderaManagerCommandCheckerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerApplyHostTemplateListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerDecommissionHostListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerDeployClientConfigListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerParcelActivationListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerGenerateCredentialsListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerHostStatusChecker;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerKerberosConfigureListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerParcelRepoChecker;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerRefreshServiceConfigsListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerRestartServicesListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerServiceStartListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerStartManagementServiceListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerStartupListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerStopListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerStopManagementServiceListenerService;
import com.sequenceiq.cloudbreak.cm.polling.service.ClouderaManagerTemplateInstallChecker;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.polling.PollingResult;
import com.sequenceiq.cloudbreak.polling.PollingService;
import com.sequenceiq.cloudbreak.polling.StatusCheckerService;

@Service
public class ClouderaManagerPollingServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClouderaManagerPollingServiceProvider.class);

    private static final int POLL_INTERVAL = 5000;

    private static final int INFINITE_ATTEMPT = -1;

    private static final int TWELVE_HOUR = 8640;

    @Inject
    private PollingService<ClouderaManagerPollerObject> clouderaManagerPollerService;

    @Inject
    private PollingService<ClouderaManagerCommandPollerObject> clouderaManagerCommandPollerObjectPollingService;

    @Inject
    private ClouderaManagerStartupListenerService clouderaManagerStartupListenerTask;

    @Inject
    private ClouderaManagerServiceStartListenerService clouderaManagerServiceStartListenerTask;

    @Inject
    private ClouderaManagerStopListenerService clouderaManagerStopListenerTask;

    @Inject
    private ClouderaManagerHostStatusChecker clouderaManagerHostStatusChecker;

    @Inject
    private ClouderaManagerTemplateInstallChecker clouderaManagerTemplateInstallChecker;

    @Inject
    private ClouderaManagerParcelRepoChecker clouderaManagerParcelRepoChecker;

    @Inject
    private ClouderaManagerKerberosConfigureListenerService kerberosConfigureListenerTask;

    @Inject
    private ClouderaManagerParcelActivationListenerService parcelActivationListenerTask;

    @Inject
    private ClouderaManagerDeployClientConfigListenerService deployClientConfigListenerTask;

    @Inject
    private ClouderaManagerApplyHostTemplateListenerService applyHostTemplateListenerTask;

    @Inject
    private ClouderaManagerDecommissionHostListenerService decommissionHostListenerTask;

    @Inject
    private ClouderaManagerStartManagementServiceListenerService startManagementServiceListenerTask;

    @Inject
    private ClouderaManagerStopManagementServiceListenerService stopManagementServiceListenerTask;

    @Inject
    private ClouderaManagerRestartServicesListenerService restartServicesListenerTask;

    @Inject
    private ClouderaManagerGenerateCredentialsListenerService generateCredentialsListenerTask;

    @Inject
    private ClouderaManagerRefreshServiceConfigsListenerService refreshServiceConfigsListenerTask;

    public PollingResult clouderaManagerStartupPollerObjectPollingService(Stack stack, ApiClient apiClient) {
        LOGGER.debug("Waiting for Cloudera Manager startup. [Server address: {}]", stack.getClusterManagerIp());
        return pollCMWithListener(stack, apiClient, clouderaManagerStartupListenerTask);
    }

    public PollingResult hostsPollingService(Stack stack, ApiClient apiClient) {
        LOGGER.debug("Waiting for Cloudera Manager hosts to connect. [Server address: {}]", stack.getClusterManagerIp());
        return pollCMWithListener(stack, apiClient, clouderaManagerHostStatusChecker);
    }

    public PollingResult templateInstallCheckerService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to install template. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, clouderaManagerTemplateInstallChecker);
    }

    public PollingResult parcelRepoRefreshCheckerService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to refresh parcel repo. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, clouderaManagerParcelRepoChecker);
    }

    public PollingResult stopPollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager services to stop. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, clouderaManagerStopListenerTask);
    }

    public PollingResult startPollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager services to start. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, clouderaManagerServiceStartListenerTask);
    }

    public PollingResult kerberosConfigurePollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to configure kerberos. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, kerberosConfigureListenerTask);
    }

    public PollingResult parcelActivationPollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to deploy client configuratuions. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, parcelActivationListenerTask);
    }

    public PollingResult deployClientConfigPollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to deploy client configuratuions. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, deployClientConfigListenerTask);
    }

    public PollingResult refreshClusterPollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to refresh cluster. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, refreshServiceConfigsListenerTask);
    }

    public PollingResult applyHostTemplatePollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to apply host template. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, applyHostTemplateListenerTask);
    }

    public PollingResult decommissionHostPollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to decommission host. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, INFINITE_ATTEMPT, decommissionHostListenerTask);
    }

    public PollingResult startManagementServicePollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to start management service. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, startManagementServiceListenerTask);
    }

    public PollingResult stopManagementServicePollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to stop management service. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, stopManagementServiceListenerTask);
    }

    public PollingResult restartServicesPollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to restart services. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, restartServicesListenerTask);
    }

    public PollingResult generateCredentialsPollingService(Stack stack, ApiClient apiClient, BigDecimal commandId) {
        LOGGER.debug("Waiting for Cloudera Manager to finish generate credentials. [Server address: {}]", stack.getClusterManagerIp());
        return pollCommandWithListener(stack, apiClient, commandId, TWELVE_HOUR, generateCredentialsListenerTask);
    }

    private PollingResult pollCMWithListener(Stack stack, ApiClient apiClient, StatusCheckerService<ClouderaManagerPollerObject> listenerTask) {
        ClouderaManagerPollerObject clouderaManagerPollerObject = new ClouderaManagerPollerObject(stack, apiClient);
        return clouderaManagerPollerService.pollWithTimeoutSingleFailure(
                listenerTask,
                clouderaManagerPollerObject,
                POLL_INTERVAL,
                TWELVE_HOUR);
    }

    private PollingResult pollCommandWithListener(Stack stack, ApiClient apiClient, BigDecimal commandId, int numAttempts,
            AbstractClouderaManagerCommandCheckerService<ClouderaManagerCommandPollerObject> listenerTask) {
        ClouderaManagerCommandPollerObject clouderaManagerPollerObject = new ClouderaManagerCommandPollerObject(stack, apiClient, commandId);
        return clouderaManagerCommandPollerObjectPollingService.pollWithTimeoutSingleFailure(
                listenerTask,
                clouderaManagerPollerObject,
                POLL_INTERVAL,
                numAttempts);
    }
}
