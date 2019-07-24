package com.sequenceiq.freeipa.kerberosmgmt.v1;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.googlecode.jsonrpc4j.JsonRpcClientException;
import com.sequenceiq.cloudbreak.service.secret.model.SecretResponse;
import com.sequenceiq.cloudbreak.service.secret.model.StringToSecretResponseConverter;
import com.sequenceiq.cloudbreak.service.secret.service.SecretService;
import com.sequenceiq.freeipa.api.v1.kerberosmgmt.model.VaultCleanupRequest;
import com.sequenceiq.freeipa.api.v1.kerberosmgmt.model.HostRequest;
import com.sequenceiq.freeipa.api.v1.kerberosmgmt.model.RoleRequest;
import com.sequenceiq.freeipa.api.v1.kerberosmgmt.model.ServiceKeytabRequest;
import com.sequenceiq.freeipa.api.v1.kerberosmgmt.model.ServiceKeytabResponse;
import com.sequenceiq.freeipa.api.v1.kerberosmgmt.model.ServicePrincipalRequest;
import com.sequenceiq.freeipa.client.FreeIpaClient;
import com.sequenceiq.freeipa.client.FreeIpaClientException;
import com.sequenceiq.freeipa.client.model.Keytab;
import com.sequenceiq.freeipa.client.model.Privilege;
import com.sequenceiq.freeipa.client.model.Role;
import com.sequenceiq.freeipa.controller.exception.NotFoundException;
import com.sequenceiq.freeipa.entity.FreeIpa;
import com.sequenceiq.freeipa.entity.Stack;
import com.sequenceiq.freeipa.kerberosmgmt.exception.KeytabCreationException;
import com.sequenceiq.freeipa.service.freeipa.FreeIpaClientFactory;
import com.sequenceiq.freeipa.service.freeipa.FreeIpaService;
import com.sequenceiq.freeipa.service.stack.StackService;

@Service
public class KerberosMgmtV1Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(KerberosMgmtV1Service.class);

    private static final String HOST_CREATION_FAILED = "Failed to create host.";

    private static final String SERVICE_PRINCIPAL_CREATION_FAILED = "Failed to create service principal.";

    private static final String SERVICE_ALLOW_FAILURE = "Request to allow the service to retrieve keytab failed.";

    private static final String KEYTAB_GENERATION_FAILED = "Failed to create keytab.";

    private static final String KEYTAB_FETCH_FAILED = "Failed to fetch keytab.";

    private static final String VAULT_UPDATE_FAILED = "Failed to update Vault.";

    private static final String EMPTY_REALM = "Failed to create service as realm was empty.";

    private static final String IPA_STACK_NOT_FOUND = "Stack for IPA server not found.";

    private static final String VAULT_SECRET_TYPE = "ServiceKeytab";

    private static final String KEYTAB_SUB_TYPE = "keytab";

    private static final String PRINCIPAL_SUB_TYPE = "serviceprincipal";

    private static final int DUPLICATE_ENTRY_ERROR_CODE = 4002;

    @Inject
    private StackService stackService;

    @Inject
    private FreeIpaService freeIpaService;

    @Inject
    private FreeIpaClientFactory freeIpaClientFactory;

    @Inject
    private SecretService secretService;

    @Inject
    private StringToSecretResponseConverter stringToSecretResponseConverter;

    public ServiceKeytabResponse generateServiceKeytab(ServiceKeytabRequest request, String accountId) throws FreeIpaClientException {
        LOGGER.debug("Request to generate keytab for Service:{} Host:{} in Environment:{}", request.getServiceName(), request.getServerHostName(),
                request.getEnvironmentCrn());
        ServiceKeytabResponse response = new ServiceKeytabResponse();
        FreeIpaClient ipaClient;
        Stack freeIpaStack = getFreeIpaStack(request.getEnvironmentCrn(), accountId);
        String realm = getRealm(freeIpaStack);
        ipaClient = freeIpaClientFactory.getFreeIpaClientForStack(freeIpaStack);
        hostAdd(request.getServerHostName(), ipaClient);
        com.sequenceiq.freeipa.client.model.Service service = serviceAdd(request, realm, ipaClient);
        String serviceKeytab;
        if (service.getHasKeytab() && request.getDoNotRecreateKeytab()) {
            serviceKeytab = getExistingServiceKeytab(service.getKrbprincipalname(), ipaClient);
        } else {
            serviceKeytab = getServiceKeytab(service.getKrbprincipalname(), ipaClient);
        }
        response.setKeytab(getSecretResponseForKeytab(request, accountId, serviceKeytab));
        response.setServicePrincipal(getSecretResponseForPrincipal(request, accountId, service.getKrbprincipalname()));
        return response;
    }

    public ServiceKeytabResponse getExistingServiceKeytab(ServiceKeytabRequest request, String accountId) throws FreeIpaClientException {
        ServiceKeytabResponse response = new ServiceKeytabResponse();
        FreeIpaClient ipaClient;
        Stack freeIpaStack = getFreeIpaStack(request.getEnvironmentCrn(), accountId);
        String realm = getRealm(freeIpaStack);
        ipaClient = freeIpaClientFactory.getFreeIpaClientForStack(freeIpaStack);

        String servicePrincipal = request.getServiceName() + "/" + request.getServerHostName() + "@" + realm;
        String serviceKeytab = getExistingServiceKeytab(servicePrincipal, ipaClient);
        response.setKeytab(getSecretResponseForKeytab(request, accountId, serviceKeytab));
        response.setServicePrincipal(getSecretResponseForPrincipal(request, accountId, servicePrincipal));
        return response;
    }

    public void deleteServicePrincipal(ServicePrincipalRequest request, String accountId) throws FreeIpaClientException {
        FreeIpaClient ipaClient;
        Stack freeIpaStack = getFreeIpaStack(request.getEnvironmentCrn(), accountId);
        String realm = getRealm(freeIpaStack);
        String canonicalPrincipal = constructCanonicalPrincipal(request.getServiceName(), request.getServerHostName(), realm);
        ipaClient = freeIpaClientFactory.getFreeIpaClientForStack(freeIpaStack);
        ipaClient.deleteService(canonicalPrincipal);
        //Delete the vault secrets associated with the service principal
        if (Strings.isNullOrEmpty(request.getClusterCrn())) {
            LOGGER.warn("Cluster CRN not provided. Auto-generating one");
            request.setClusterCrn(generateClusterCrn(accountId, request.getEnvironmentCrn()));
        }
        cleanupByServicePrincipal(getVaultPathPrefixForPrincipal(request, accountId, PRINCIPAL_SUB_TYPE));
        cleanupByServicePrincipal(getVaultPathPrefixForPrincipal(request, accountId, KEYTAB_SUB_TYPE));
    }

    public void deleteHost(HostRequest request, String accountId) throws FreeIpaClientException {
        FreeIpaClient ipaClient;
        Stack freeIpaStack = getFreeIpaStack(request.getEnvironmentCrn(), accountId);
        ipaClient = freeIpaClientFactory.getFreeIpaClientForStack(freeIpaStack);

        Set<String> services = ipaClient.findAllService().stream()
                .filter(s -> s.getKrbprincipalname().contains(request.getServerHostName()))
                .map(f -> f.getKrbcanonicalname()).collect(Collectors.toSet());
        LOGGER.debug("Services count on the given host: {}", services.size());
        for (String service : services) {
            ipaClient.deleteService(service);
        }
        ipaClient.deleteHost(request.getServerHostName());
        //Delete the vault secrets associated with the host
        if (Strings.isNullOrEmpty(request.getClusterCrn())) {
            LOGGER.warn("Cluster CRN not provided. Auto-generating one");
            request.setClusterCrn(generateClusterCrn(accountId, request.getEnvironmentCrn()));
        }
        cleanupByHost(getVaultPathPrefixForHost(request, accountId, PRINCIPAL_SUB_TYPE));
        cleanupByHost(getVaultPathPrefixForHost(request, accountId, KEYTAB_SUB_TYPE));
    }

    public void cleanupByCluster(VaultCleanupRequest request, String accountId) {
        if (Strings.isNullOrEmpty(request.getClusterCrn())) {
            LOGGER.warn("Cluster CRN not provided. Vault is not cleaned-up");
            return;
        }
        String keytabPrefix = getVaultPathPrefixForCluster(request, accountId, KEYTAB_SUB_TYPE);
        // Sample pathPrefix: "/enginePath/appPath/account-id/Type/SubType/envCrn/clusterCrn"
        secretService.listEntries(keytabPrefix).stream().forEach(path -> {
            String newPath = keytabPrefix + "/" + path;
            cleanupByHost(newPath);
        });
        String servicePrincipalPrefix = getVaultPathPrefixForCluster(request, accountId, PRINCIPAL_SUB_TYPE);
        secretService.listEntries(servicePrincipalPrefix).stream().forEach(path -> {
            String newPath = servicePrincipalPrefix + "/" + path;
            cleanupByHost(newPath);
        });
    }

    /**
     * Identifies all the secrets in the hostPath and deletes them
     * @param hostPath Path for a particular host in a cluster.
     */
    public void cleanupByHost(String hostPath) {
        // Sample hostPath: "/enginePath/appPath/account-id/Type/SubType/envCrn/clusterCrn/hostname"
        LOGGER.debug("Looking for the service princiapls unser path: " + hostPath);
        secretService.listEntries(hostPath).stream().forEach(path -> {
            String newPath = hostPath + (hostPath.endsWith("/") ? "" : "/") + path;
            cleanupByServicePrincipal(newPath);
        });
    }

    /**
     * Identifies all the secrets in the servicePrincipalPath and deletes them.
     * @param servicePrincipalPath Path for particular service in given host in a cluster.
     *
     * This is where the vault secrets are stored.
     */
    public void cleanupByServicePrincipal(String servicePrincipalPath) {
        // Sample pathPrefix: "/enginePath/appPath/account-id/Type/SubType/envCrn/clusterCrn/hostname/serviceName"
        LOGGER.debug("Performing a cleanup of all the secrets located under path: " + servicePrincipalPath);
        secretService.cleanup(servicePrincipalPath);
    }

    public List<String> list(String pathPrefix) {
        return secretService.listEntries(pathPrefix);
    }

    private Stack getFreeIpaStack(String envCrn, String accountId) {
        LOGGER.debug("Looking for stack using env:{} and account-id:{}", envCrn, accountId);
        return stackService.getByEnvironmentCrnAndAccountId(envCrn, accountId);
    }

    private String getRealm(Stack stack) {
        try {
            FreeIpa freeIpa = freeIpaService.findByStack(stack);
            if (!Strings.isNullOrEmpty(freeIpa.getDomain())) {
                LOGGER.debug("Realm of IPA Server: {}", freeIpa.getDomain().toUpperCase());
                return freeIpa.getDomain().toUpperCase();
            }
        } catch (NotFoundException notfound) {
            LOGGER.error("Realm not found");
        }
        throw new KeytabCreationException(EMPTY_REALM);
    }

    private void hostAdd(String hostname, FreeIpaClient ipaClient) throws KeytabCreationException {
        try {
            ipaClient.addHost(hostname);
        } catch (FreeIpaClientException e) {
            if (!isDuplicateEntryException(e)) {
                LOGGER.error(HOST_CREATION_FAILED + " " + e.getLocalizedMessage(), e);
                throw new KeytabCreationException(HOST_CREATION_FAILED);
            }
        }
    }

    private com.sequenceiq.freeipa.client.model.Service serviceAdd(ServiceKeytabRequest request, String realm, FreeIpaClient ipaClient)
            throws KeytabCreationException {
        String canonicalPrincipal = constructCanonicalPrincipal(request.getServiceName(), request.getServerHostName(), realm);
        com.sequenceiq.freeipa.client.model.Service service;
        try {
            try {
                service = ipaClient.addService(canonicalPrincipal);
            } catch (FreeIpaClientException e) {
                if (!isDuplicateEntryException(e)) {
                    throw e;
                }
                service = ipaClient.showService(canonicalPrincipal);
            }
            allowServiceKeytabRetrieval(service.getKrbprincipalname(), freeIpaClientFactory.getAdminUser(), ipaClient);
            addRoleAndPrivileges(service, request.getRoleRequest(), ipaClient);
        } catch (FreeIpaClientException e) {
            LOGGER.error(SERVICE_PRINCIPAL_CREATION_FAILED + " " + e.getLocalizedMessage(), e);
            throw new KeytabCreationException(SERVICE_PRINCIPAL_CREATION_FAILED);
        }
        return service;
    }

    private void addRoleAndPrivileges(com.sequenceiq.freeipa.client.model.Service service, RoleRequest roleRequest, FreeIpaClient ipaClient)
            throws FreeIpaClientException {
        if (roleRequest != null && StringUtils.isNotBlank(roleRequest.getRoleName())) {
            Set<Role> allRole = ipaClient.findAllRole();
            Optional<Role> optionalRole = allRole.stream().filter(role -> role.getCn().equals(roleRequest.getRoleName())).findFirst();
            Role role = optionalRole.isPresent() ? optionalRole.get() : ipaClient.addRole(roleRequest.getRoleName());
            addPrivilegesToRole(roleRequest.getPrivileges(), ipaClient, role);
            role = ipaClient.showRole(role.getCn());
            boolean roleSetForService = service.getMemberOfRole().stream().anyMatch(member -> member.contains(roleRequest.getRoleName()));
            if (!roleSetForService) {
                ipaClient.addRoleMember(role.getCn(), null, null, null, null, Set.of(service.getKrbprincipalname()));
            }
        }
    }

    private void addPrivilegesToRole(Set<String> privileges, FreeIpaClient ipaClient, Role role) throws FreeIpaClientException {
        if (privileges != null) {
            Set<String> privilegesToAdd = privileges.stream().filter(privilegeName -> {
                try {
                    Privilege privilege = ipaClient.showPrivilege(privilegeName);
                    return privilege.getMember().stream().noneMatch(member -> member.equals(role.getCn()));
                } catch (FreeIpaClientException e) {
                    LOGGER.error("Privilege [{}] show error", privilegeName, e);
                    return false;
                }
            }).collect(Collectors.toSet());
            if (!privilegesToAdd.isEmpty()) {
                ipaClient.addRolePriviliges(role.getCn(), privilegesToAdd);
            }
        }
    }

    private void allowServiceKeytabRetrieval(String canonicalPrincipal, String adminUser, FreeIpaClient ipaClient) throws FreeIpaClientException {
        try {
            ipaClient.allowServiceKeytabRetrieval(canonicalPrincipal, adminUser);
        } catch (FreeIpaClientException e) {
            LOGGER.error(SERVICE_ALLOW_FAILURE + " " + e.getLocalizedMessage(), e);
            throw e;
        }
    }

    private String getServiceKeytab(String canonicalPrincipal, FreeIpaClient ipaClient) throws KeytabCreationException {
        try {
            Keytab keytab = ipaClient.getKeytab(canonicalPrincipal);
            return keytab.getKeytab();
        } catch (FreeIpaClientException e) {
            LOGGER.error(KEYTAB_GENERATION_FAILED + " " + e.getLocalizedMessage(), e);
            throw new KeytabCreationException(KEYTAB_GENERATION_FAILED);
        }
    }

    private String getExistingServiceKeytab(String canonicalPrincipal, FreeIpaClient ipaClient) throws KeytabCreationException {
        try {
            Keytab keytab = ipaClient.getExistingKeytab(canonicalPrincipal);
            return keytab.getKeytab();
        } catch (FreeIpaClientException e) {
            LOGGER.error(KEYTAB_FETCH_FAILED + " " + e.getLocalizedMessage(), e);
            throw new KeytabCreationException(KEYTAB_FETCH_FAILED);
        }
    }

    private boolean isDuplicateEntryException(FreeIpaClientException e) {
        return Optional.ofNullable(e.getCause())
                .filter(JsonRpcClientException.class::isInstance)
                .map(JsonRpcClientException.class::cast)
                .map(JsonRpcClientException::getCode)
                .filter(c -> c == DUPLICATE_ENTRY_ERROR_CODE)
                .isPresent();
    }

    private SecretResponse getSecretResponseForPrincipal(ServiceKeytabRequest request, String accountId, String principal) {
        try {
            String path = constructVaultPath(request, accountId, VAULT_SECRET_TYPE, PRINCIPAL_SUB_TYPE);
            String secret = secretService.put(path, principal);
            return stringToSecretResponseConverter.convert(secret);
        } catch (Exception exception) {
            LOGGER.warn("Failure while updating vault.", exception);
            throw new KeytabCreationException(VAULT_UPDATE_FAILED);
        }
    }

    private SecretResponse getSecretResponseForKeytab(ServiceKeytabRequest request, String accountId, String keytab) {
        try {
            String path = constructVaultPath(request, accountId, VAULT_SECRET_TYPE, KEYTAB_SUB_TYPE);
            String secret = secretService.put(path, keytab);
            return stringToSecretResponseConverter.convert(secret);
        } catch (Exception exception) {
            LOGGER.warn("Failure while updating vault.", exception);
            throw new KeytabCreationException(VAULT_UPDATE_FAILED);
        }
    }

    private String getVaultPathPrefixForCluster(VaultCleanupRequest request, String accountId, String subType) {
        return String.format("%s/%s/%s/%s/%s", accountId, VAULT_SECRET_TYPE, subType,
                request.getEnvironmentCrn(), request.getClusterCrn());
    }

    private String getVaultPathPrefixForHost(HostRequest request, String accountId, String subType) {
        return String.format("%s/%s/%s/%s/%s/%s", accountId, VAULT_SECRET_TYPE, subType,
                request.getEnvironmentCrn(), request.getClusterCrn(), request.getServerHostName());
    }

    private String getVaultPathPrefixForPrincipal(ServicePrincipalRequest request, String accountId, String subType) {
        return String.format("%s/%s/%s/%s/%s/%s/%s", accountId, VAULT_SECRET_TYPE, subType,
                request.getEnvironmentCrn(), request.getClusterCrn(), request.getServerHostName(),
                request.getServiceName());
    }

    private String constructVaultPath(ServiceKeytabRequest request, String accountId, String type, String subtype) {
        // Sample Vault Path "/enginePath/appPath/account-id/Type/SubType/envCrn/clusterCrn/hostname/serviceName"
        String clusterCrn;
        if (request.getClusterCrn() == null) {
            LOGGER.warn("Cluster CRN not provided. Auto-generating one");
            clusterCrn = generateClusterCrn(accountId, request.getEnvironmentCrn());
        } else {
            clusterCrn = request.getClusterCrn();
        }
        return String.format("%s/%s/%s/%s/%s/%s/%s", accountId, type, subtype,
                request.getEnvironmentCrn(), clusterCrn, request.getServerHostName(),
                request.getServiceName());
    }

    private String generateClusterCrn(String accountId, String envCrn) {
        return accountId + "-" + envCrn;
    }

    private static String constructCanonicalPrincipal(String serviceName, String hostName, String realm) {
        return serviceName + "/" + hostName + "@" + realm;
    }
}
