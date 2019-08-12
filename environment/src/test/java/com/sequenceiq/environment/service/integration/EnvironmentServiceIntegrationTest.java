package com.sequenceiq.environment.service.integration;

import static com.sequenceiq.environment.proxy.v1.ProxyTestSource.getProxyConfig;
import static com.sequenceiq.environment.proxy.v1.ProxyTestSource.getProxyRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import com.sequenceiq.authorization.service.UmsAuthorizationService;
import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.event.credential.CredentialVerificationRequest;
import com.sequenceiq.cloudbreak.cloud.event.credential.CredentialVerificationResult;
import com.sequenceiq.cloudbreak.cloud.event.credential.InitCodeGrantFlowRequest;
import com.sequenceiq.cloudbreak.cloud.event.credential.InteractiveLoginRequest;
import com.sequenceiq.cloudbreak.cloud.event.credential.InteractiveLoginResult;
import com.sequenceiq.cloudbreak.cloud.event.platform.CloudReactorRequestProvider;
import com.sequenceiq.cloudbreak.cloud.event.platform.GetPlatformRegionsRequestV2;
import com.sequenceiq.cloudbreak.cloud.event.platform.GetPlatformRegionsResultV2;
import com.sequenceiq.cloudbreak.cloud.event.platform.ResourceDefinitionRequest;
import com.sequenceiq.cloudbreak.cloud.event.platform.ResourceDefinitionResult;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredentialStatus;
import com.sequenceiq.cloudbreak.cloud.model.CloudRegions;
import com.sequenceiq.cloudbreak.cloud.model.CredentialStatus;
import com.sequenceiq.cloudbreak.service.secret.service.SecretService;
import com.sequenceiq.environment.api.v1.credential.model.parameters.aws.AwsCredentialParameters;
import com.sequenceiq.environment.api.v1.credential.model.parameters.aws.KeyBasedParameters;
import com.sequenceiq.environment.api.v1.credential.model.parameters.azure.AzureCredentialRequestParameters;
import com.sequenceiq.environment.api.v1.credential.model.parameters.azure.RoleBasedRequest;
import com.sequenceiq.environment.api.v1.credential.model.request.CredentialRequest;
import com.sequenceiq.environment.api.v1.credential.model.response.CredentialResponse;
import com.sequenceiq.environment.api.v1.credential.model.response.CredentialResponses;
import com.sequenceiq.environment.api.v1.credential.model.response.InteractiveCredentialResponse;
import com.sequenceiq.environment.api.v1.environment.model.request.EnvironmentAuthenticationRequest;
import com.sequenceiq.environment.api.v1.environment.model.request.EnvironmentNetworkRequest;
import com.sequenceiq.environment.api.v1.environment.model.request.EnvironmentRequest;
import com.sequenceiq.environment.api.v1.environment.model.request.LocationRequest;
import com.sequenceiq.environment.api.v1.environment.model.request.SecurityAccessRequest;
import com.sequenceiq.environment.api.v1.environment.model.response.DetailedEnvironmentResponse;
import com.sequenceiq.environment.api.v1.proxy.model.request.ProxyRequest;
import com.sequenceiq.environment.api.v1.proxy.model.response.ProxyResponse;
import com.sequenceiq.environment.api.v1.proxy.model.response.ProxyResponses;
import com.sequenceiq.environment.client.EnvironmentServiceClientBuilder;
import com.sequenceiq.environment.client.EnvironmentServiceCrnEndpoints;
import com.sequenceiq.environment.credential.domain.Credential;
import com.sequenceiq.environment.credential.repository.CredentialRepository;
import com.sequenceiq.environment.credential.service.RequestProvider;
import com.sequenceiq.environment.network.NetworkService;
import com.sequenceiq.environment.environment.repository.EnvironmentRepository;
import com.sequenceiq.environment.environment.service.EnvironmentTestData;
import com.sequenceiq.environment.proxy.domain.ProxyConfig;
import com.sequenceiq.environment.proxy.repository.ProxyConfigRepository;
import com.sequenceiq.environment.service.integration.testconfiguration.TestConfigurationForServiceIntegration;
import com.sequenceiq.flow.reactor.ErrorHandlerAwareReactorEventFactory;
import com.sequenceiq.flow.reactor.api.event.BaseFlowEvent;

import reactor.bus.Event;
import reactor.rx.Promise;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestConfigurationForServiceIntegration.class)
@ActiveProfiles("test")
public class EnvironmentServiceIntegrationTest {

    private static final String SERVICE_ADDRESS = "http://localhost:%d/environmentservice";

    private static final String DEFINITION_AWS = "{\"values\":[{\"name\":\"smartSenseId\",\"type\":\"String\",\"sensitive\":false,\"optional\":true},"
            + "{\"name\":\"govCloud\",\"type\":\"String\",\"sensitive\":false,\"optional\":true}],\"selectors\":[{\"name\":\"role-based\",\"values\":"
            + "[{\"name\":\"roleArn\",\"type\":\"String\"},{\"name\":\"externalId\",\"type\":\"String\",\"optional\":true}]},"
            + "{\"name\":\"key-based\",\"values\":[{\"name\":\"accessKey\",\"type\":\"String\"},{\"name\":\"secretKey\",\"type\":\"String\"}]}]}";

    private static final String TEST_ACCOUNT_ID = "accid";

    private static final String TEST_CRN = String.format("crn:altus:iam:us-west-1:%s:user:mockuser@cloudera.com", TEST_ACCOUNT_ID);

    private static final String USER_CODE = "1234";

    private static final String VERIFICATION_URL = "http://cloudera.com";

    private EnvironmentServiceCrnEndpoints client;

    @LocalServerPort
    private int port;

    @Mock
    private ResourceDefinitionRequest resourceDefinitionRequest;

    @Mock
    private InteractiveLoginRequest interactiveLoginRequest;

    @Mock
    private InitCodeGrantFlowRequest initCodeGrantFlowRequest;

    @Mock
    private GetPlatformRegionsRequestV2 getPlatformRegionsRequestV2;

    @MockBean
    private RequestProvider environmentRequestProvider;

    @MockBean
    private CloudReactorRequestProvider cloudReactorRequestProvider;

    @MockBean
    private ErrorHandlerAwareReactorEventFactory eventFactory;

    @MockBean
    private UmsAuthorizationService umsAuthorizationService;

    @MockBean
    private NetworkService networkService;

    @Inject
    private ProxyConfigRepository proxyConfigRepository;

    @Inject
    private CredentialRepository credentialRepository;

    @Inject
    private EnvironmentRepository environmentRepository;

    @Inject
    private SecretService secretService;

    private CredentialRequest credentialRequest;

    private Credential credential;

    @BeforeEach
    public void setup() {
        client = new EnvironmentServiceClientBuilder(String.format(SERVICE_ADDRESS, port))
                .withCertificateValidation(false)
                .withDebug(true)
                .withIgnorePreValidation(true)
                .build()
                .withCrn(TEST_CRN);

        credential = new Credential();
        credential.setName("credential_test");
        credential.setResourceCrn("credential_resourcecrn");
        credential.setAccountId(TEST_ACCOUNT_ID);
        credential.setCloudPlatform("AWS");
        credential.setCreator("user");
        credential.setDescription("description");
        credential.setGovCloud(false);
        credential.setArchived(false);
        credentialRequest = new CredentialRequest();

        doNothing().when(umsAuthorizationService).checkRightOfUserForResource(any(), any(), any(), any());
    }

    @AfterEach
    public void clienUpDb() {
        environmentRepository.deleteAll();
        proxyConfigRepository.deleteAll();
        credentialRepository.deleteAll();
    }

    @Test
    public void testEnvironmentCreate() throws InterruptedException {
        createAwsCredential("testcredential");
        EnvironmentRequest request = getEnvironmentRequest();

        when(cloudReactorRequestProvider.getPlatformRegionsRequestV2(any(), any(), any(), any(), any()))
                .thenReturn(getPlatformRegionsRequestV2);
        CloudRegions cloudRegions = EnvironmentTestData.getCloudRegions();
        when(getPlatformRegionsRequestV2.await())
                .thenReturn(new GetPlatformRegionsResultV2(1L, cloudRegions));
        DetailedEnvironmentResponse result = client.environmentV1Endpoint().post(request);
    }

    private EnvironmentRequest getEnvironmentRequest() {
        EnvironmentRequest request = new EnvironmentRequest();
        request.setCredentialName("testcredential");
        request.setName("testenvironment");
        request.setCloudPlatform("AWS");
        request.setDescription("testdescription");
        EnvironmentAuthenticationRequest authenticationRequest = new EnvironmentAuthenticationRequest();
        authenticationRequest.setPublicKeyId("testkeyid");
        authenticationRequest.setPublicKey("ssh-rsa"); //TODO most NOT NULL, de ez tuti hogy lehet null (pl aws)
        request.setAuthentication(authenticationRequest);
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setName("r1");
        request.setLocation(locationRequest);
        request.setRegions(Set.of("r1"));
        EnvironmentNetworkRequest networkRequest = new EnvironmentNetworkRequest();
        networkRequest.setNetworkCidr("0.0.0.0/0");
        request.setNetwork(networkRequest);
        SecurityAccessRequest securityAccess = new SecurityAccessRequest();
        securityAccess.setCidr("0.0.0.0/0");
        request.setSecurityAccess(securityAccess);
        return request;
    }

    private void createAwsCredential(String name) throws InterruptedException {
        credentialRequest.setAws(getAwsKeyBasedCredentialParameters(false, "yyy", "zzzz"));
        credentialRequest.setCloudPlatform("AWS");
        credentialRequest.setName(name);

        when(environmentRequestProvider.getResourceDefinitionRequest(any(), any())).thenReturn(resourceDefinitionRequest);
        when(environmentRequestProvider.getCredentialVerificationRequest(any(), any())).thenAnswer(
                invocation -> new CredentialVerificationMockRequest(invocation.getArgument(0), invocation.getArgument(1))
        );
        when(resourceDefinitionRequest.await()).thenReturn(new ResourceDefinitionResult(1L, DEFINITION_AWS));

        Promise<Boolean> thisisarealtrue = new Promise<Boolean>();
        thisisarealtrue.accept(Boolean.TRUE);
        when(eventFactory.createEventWithErrHandler(any(), any()))
                .thenReturn(new Event<>(new BaseFlowEvent("", 1L, "", thisisarealtrue)));

        CredentialResponse response = client.credentialV1Endpoint().post(credentialRequest);
    }

    @Test
    public void testCredentialCreateAws() throws InterruptedException {
        credentialRequest.setAws(getAwsKeyBasedCredentialParameters(false, "yyy", "zzzz"));
        credentialRequest.setCloudPlatform("AWS");
        credentialRequest.setName("testcredential");

        when(environmentRequestProvider.getResourceDefinitionRequest(any(), any())).thenReturn(resourceDefinitionRequest);
        when(environmentRequestProvider.getCredentialVerificationRequest(any(), any())).thenAnswer(
                invocation -> new CredentialVerificationMockRequest(invocation.getArgument(0), invocation.getArgument(1))
        );
        when(resourceDefinitionRequest.await()).thenReturn(new ResourceDefinitionResult(1L, DEFINITION_AWS));

        CredentialResponse response = client.credentialV1Endpoint().post(credentialRequest);
        assertTrue(response.getName().equals(credentialRequest.getName()), " not saved, or response is different");
        assertTrue(credentialRepository.findByNameAndAccountId(credentialRequest.getName(), TEST_ACCOUNT_ID, List.of("AWS")).isPresent());
    }

    @Test
    public void testCredentialInteractiveLogin() throws InterruptedException {
        credentialRequest.setName("testcredential");
        credentialRequest.setCloudPlatform("AZURE");
        AzureCredentialRequestParameters azureCredentialRequestParameters = new AzureCredentialRequestParameters();
        azureCredentialRequestParameters.setSubscriptionId("subid");
        azureCredentialRequestParameters.setTenantId("tenant");
        RoleBasedRequest roleBasedRequest = new RoleBasedRequest();
        roleBasedRequest.setDeploymentAddress("alma");
        roleBasedRequest.setRoleName("role");
        azureCredentialRequestParameters.setRoleBased(roleBasedRequest);
        credentialRequest.setAzure(azureCredentialRequestParameters);

        InteractiveLoginResult interactiveLoginResult = new InteractiveLoginResult(1L, Map.of("user_code", USER_CODE, "verification_url", VERIFICATION_URL));
        when(environmentRequestProvider.getInteractiveLoginRequest(any(), any())).thenReturn(interactiveLoginRequest);
        when(interactiveLoginRequest.await()).thenReturn(interactiveLoginResult);
        InteractiveCredentialResponse result = client.credentialV1Endpoint().interactiveLogin(credentialRequest);
        assertEquals(result.getUserCode(), USER_CODE);
        assertEquals(result.getVerificationUrl(), VERIFICATION_URL);
    }

/*
    TODO: finish grant code tests
    @Test
    public void testCredentialInitCodeGrantFlow() throws InterruptedException {
        credentialRequest.setName("testcredential");
        credentialRequest.setCloudPlatform("AZURE");
        AzureCredentialRequestParameters azureCredentialRequestParameters = new AzureCredentialRequestParameters();
        azureCredentialRequestParameters.setSubscriptionId("subid");
        azureCredentialRequestParameters.setTenantId("tenant");
        RoleBasedRequest roleBasedRequest = new RoleBasedRequest();
        roleBasedRequest.setDeploymentAddress("alma");
        roleBasedRequest.setRoleName("role");
        azureCredentialRequestParameters.setRoleBased(roleBasedRequest);
        credentialRequest.setAzure(azureCredentialRequestParameters);

        InitCodeGrantFlowResponse initCodeGrantFlowResponse = new InitCodeGrantFlowResponse(1L, Map.of());

        when(environmentRequestProvider.getInitCodeGrantFlowRequest(any(), any())).thenReturn(initCodeGrantFlowRequest);
        when(initCodeGrantFlowRequest.await()).thenReturn(initCodeGrantFlowResponse);
        Response result = client.credentialV1Endpoint().initCodeGrantFlow(credentialRequest);
    }*/

    @Test
    public void testCredentialList() {
        credentialRepository.save(credential);
        CredentialResponses resuls = client.credentialV1Endpoint().list();
        assertTrue(resuls.getResponses().stream().anyMatch(credentialResponse -> credentialResponse.getName().equals(credential.getName())),
                String.format("Result set should have credential with name: %s", credential.getName()));
    }

    @Test
    public void testCredentialGetByName() {
        credentialRepository.save(credential);
        CredentialResponse resuls = client.credentialV1Endpoint().getByName(credential.getName());
        assertTrue(resuls.getName().equals(credential.getName()),
                String.format("Result should have credential with name: %s", credential.getName()));
    }

    @Test
    public void testCredentialGetByNameNotFound() {
        assertThrows(NotFoundException.class, () -> client.credentialV1Endpoint().getByName("nonexisting"));
    }

    @Test
    public void testCredentialGetByCrn() {
        credentialRepository.save(credential);
        CredentialResponse resuls = client.credentialV1Endpoint().getByResourceCrn(credential.getResourceCrn());
        assertTrue(resuls.getName().equals(credential.getName()),
                String.format("Result should have credential with name: %s", credential.getName()));
    }

    @Test
    public void testCredentialGetByCrnNotFound() {
        assertThrows(NotFoundException.class, () -> client.credentialV1Endpoint().getByResourceCrn("nonexisting"));
    }

    @Test
    public void testCredentialDeleteByName() {
        credentialRepository.save(credential);
        CredentialResponse resuls = client.credentialV1Endpoint().deleteByName(credential.getName());
        assertTrue(resuls.getName().startsWith(credential.getName()),
                String.format("Result should have credential with name: %s", credential.getName()));
    }

    @Test
    public void testCredentialDeleteByNameNotFound() {
        assertThrows(NotFoundException.class, () -> client.credentialV1Endpoint().deleteByName("nonexisting"));
    }

    @Test
    public void testCredentialDeleteByCrn() {
        credentialRepository.save(credential);
        CredentialResponse resuls = client.credentialV1Endpoint().deleteByResourceCrn(credential.getResourceCrn());
        assertTrue(resuls.getName().startsWith(credential.getName()),
                String.format("Result should have credential with name: %s", credential.getName()));
    }

    @Test
    public void testCredentialDeleteByCrnNotFound() {
        assertThrows(NotFoundException.class, () -> client.credentialV1Endpoint().deleteByResourceCrn("nonexisting"));
    }

    @Test
    public void testProxyList() {
        proxyConfigRepository.save(getProxyConfig());
        ProxyResponses resuls = client.proxyV1Endpoint().list();
        assertTrue(resuls.getResponses().stream().anyMatch(proxyResponse -> proxyResponse.getName().equals(getProxyConfig().getName())),
                String.format("Result set should have proxy with name: %s", getProxyConfig().getName()));
    }

    @Test
    public void testProxyGetByName() {
        proxyConfigRepository.save(getProxyConfig());
        ProxyResponse resuls = client.proxyV1Endpoint().getByName(getProxyRequest().getName());
        assertTrue(resuls.getName().equals(getProxyConfig().getName()),
                String.format("Result should have proxy with name: %s", getProxyConfig().getName()));
    }

    @Test
    public void testProxyGetByNameNotFound() {
        assertThrows(NotFoundException.class, () -> client.proxyV1Endpoint().getByName("nonexisting"));
    }

    @Test
    public void testProxyGetByCrnName() {
        proxyConfigRepository.save(getProxyConfig());
        ProxyResponse resuls = client.proxyV1Endpoint().getByResourceCrn(getProxyConfig().getResourceCrn());
        assertTrue(resuls.getCrn().equals(getProxyConfig().getResourceCrn()),
                String.format("Result should have proxy with resource crn: %s", getProxyConfig().getResourceCrn()));
    }

    @Test
    public void testProxyGetByCrnNotFound() {
        assertThrows(NotFoundException.class, () -> client.proxyV1Endpoint().getByResourceCrn("nonexisting"));
    }

    @Test
    public void testProxyDeleteByName() {
        proxyConfigRepository.save(getProxyConfig());
        ProxyResponse resuls = client.proxyV1Endpoint().deleteByName(getProxyRequest().getName());
        assertTrue(resuls.getName().equals(getProxyConfig().getName()),
                String.format("Result should have proxy with name: %s", getProxyConfig().getName()));
    }

    @Test
    public void testProxyDeleteByNameNotFound() {
        assertThrows(NotFoundException.class, () -> client.proxyV1Endpoint().deleteByName("nonexisting"));
    }

    @Test
    public void testProxyDeleteByCrnName() {
        proxyConfigRepository.save(getProxyConfig());
        ProxyResponse resuls = client.proxyV1Endpoint().deleteByCrn(getProxyConfig().getResourceCrn());
        assertTrue(resuls.getCrn().equals(getProxyConfig().getResourceCrn()),
                String.format("Result should have proxy with resource crn: %s", getProxyConfig().getResourceCrn()));
    }

    @Test
    public void testProxyDeleteByCrnNotFound() {
        assertThrows(NotFoundException.class, () -> client.proxyV1Endpoint().deleteByCrn("nonexisting"));
    }

    @Test
    public void testProxyCreate() throws Exception {
        ProxyRequest request = getProxyRequest();
        request.setPort(8080);
        ProxyResponse result = client.proxyV1Endpoint().post(request);
        assertEquals(request.getName(), result.getName());
        Optional<ProxyConfig> saved = proxyConfigRepository.findByNameInAccount(request.getName(), TEST_ACCOUNT_ID);
        assertTrue(saved.isPresent());
    }

    @Test
    public void testProxyCreateSwaggerError() throws Exception {
        ProxyRequest request = getProxyRequest();
        request.setPort(0);
        assertThrows(BadRequestException.class, () -> client.proxyV1Endpoint().post(request));
    }

    private AwsCredentialParameters getAwsKeyBasedCredentialParameters(boolean govCloud, String yyy, String zzzz) {
        AwsCredentialParameters aws = new AwsCredentialParameters();
        aws.setGovCloud(govCloud);
        KeyBasedParameters keyBased = new KeyBasedParameters();
        keyBased.setAccessKey(yyy);
        keyBased.setSecretKey(zzzz);
        aws.setKeyBased(keyBased);
        return aws;
    }

    static class CredentialVerificationMockRequest extends CredentialVerificationRequest {

        CredentialVerificationMockRequest(CloudContext cloudContext, CloudCredential cloudCredential) {
            super(cloudContext, cloudCredential);
        }

        @Override
        public CredentialVerificationResult await() {
            return new CredentialVerificationResult(1L,
                    new CloudCredentialStatus(getCloudCredential(), CredentialStatus.CREATED));
        }
    }
}
