package com.sequenceiq.freeipa.service.stack;

import com.sequenceiq.cloudbreak.client.HttpClientConfig;
import com.sequenceiq.cloudbreak.clusterproxy.ClientCertificate;
import com.sequenceiq.cloudbreak.clusterproxy.ClusterProxyRegistrationClient;
import com.sequenceiq.cloudbreak.clusterproxy.ClusterServiceConfig;
import com.sequenceiq.cloudbreak.clusterproxy.ConfigRegistrationRequest;
import com.sequenceiq.cloudbreak.clusterproxy.ConfigRegistrationResponse;
import com.sequenceiq.cloudbreak.orchestrator.model.GatewayConfig;
import com.sequenceiq.cloudbreak.service.secret.model.SecretResponse;
import com.sequenceiq.freeipa.entity.FreeIpa;
import com.sequenceiq.freeipa.entity.Stack;
import com.sequenceiq.freeipa.service.GatewayConfigService;
import com.sequenceiq.freeipa.service.TlsSecurityService;
import com.sequenceiq.freeipa.service.freeipa.FreeIpaService;
import com.sequenceiq.freeipa.vault.FreeIpaCertVaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class ClusterProxyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterProxyService.class);

    private static final String SERVICE_NAME = "freeipa";

    @Inject
    private FreeIpaService freeIpaService;

    @Inject
    private GatewayConfigService gatewayConfigService;

    @Inject
    private ClusterProxyRegistrationClient clusterProxyRegistrationClient;

    @Inject
    private FreeIpaCertVaultComponent freeIpaCertVaultComponent;

    @Inject
    private TlsSecurityService tlsSecurityService;

    public ConfigRegistrationResponse registerFreeIpa(Long stackId) {
        LOGGER.debug("Registering cluster with cluster-proxy: StackId = [{}]", stackId);
        return registerFreeIpa(freeIpaService.findByStackId(stackId));
    }

    public ConfigRegistrationResponse registerFreeIpa(FreeIpa freeIpa) {
        Stack stack = freeIpa.getStack();

        GatewayConfig primaryGatewayConfig = gatewayConfigService.getPrimaryGatewayConfig(stack);
        HttpClientConfig httpClientConfig = tlsSecurityService.buildTLSClientConfigForPrimaryGateway(
                stack.getId(), primaryGatewayConfig.getGatewayUrl());

        LOGGER.debug("Registering cluster with cluster-proxy: Environment CRN = [{}], Stack CRN = [{}]", stack.getEnvironmentCrn(), stack.getResourceCrn());
        List<ClusterServiceConfig> serviceConfigs = List.of(createServiceConfig(stack, httpClientConfig));
        LOGGER.debug("Registering service configs [{}]", serviceConfigs);
        ConfigRegistrationRequest request = new ConfigRegistrationRequest(stack.getResourceCrn(), List.of(), serviceConfigs, null);
        return clusterProxyRegistrationClient.registerConfig(request);
    }

    public void deregisterFreeIpa(Long stackId) {
        LOGGER.debug("Deregistering cluster with cluster-proxy: StackId = [{}]", stackId);
        deregisterFreeIpa(freeIpaService.findByStackId(stackId));
    }

    public void deregisterFreeIpa(FreeIpa freeIpa) {
        Stack stack = freeIpa.getStack();
        LOGGER.debug("Deregistering cluster with cluster-proxy: Environment CRN = [{}], Stack CRN = [{}]", stack.getEnvironmentCrn(), stack.getResourceCrn());
        clusterProxyRegistrationClient.deregisterConfig(stack.getResourceCrn());
        LOGGER.debug("Cleaning up vault secrets for cluster-proxy");
        freeIpaCertVaultComponent.cleanupSecrets(stack);
    }

    private ClusterServiceConfig createServiceConfig(Stack stack, HttpClientConfig httpClientConfig) {
        LOGGER.debug("Putting vault secret for cluster-proxy");
        SecretResponse clientCertificateSercret =
            freeIpaCertVaultComponent.putGatewayClientCertificate(stack, httpClientConfig.getClientCert());
        SecretResponse clientKeySecret =
            freeIpaCertVaultComponent.putGatewayClientKey(stack, httpClientConfig.getClientKey());
        return new ClusterServiceConfig(SERVICE_NAME, List.of(httpClientConfig.getApiAddress()), List.of(),
            new ClientCertificate(clientKeySecret.getSecretPath(), clientCertificateSercret.getSecretPath()));
    }
}
