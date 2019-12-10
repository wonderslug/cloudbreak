package com.sequenceiq.it.cloudbreak.testcase.mock.perf;

import com.sequenceiq.environment.api.v1.environment.model.response.EnvironmentStatus;
import com.sequenceiq.it.cloudbreak.EnvironmentClient;
import com.sequenceiq.it.cloudbreak.client.CredentialTestClient;
import com.sequenceiq.it.cloudbreak.client.EnvironmentTestClient;
import com.sequenceiq.it.cloudbreak.context.Description;
import com.sequenceiq.it.cloudbreak.context.MockedTestContext;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.credential.CredentialTestDto;
import com.sequenceiq.it.cloudbreak.dto.environment.EnvironmentTestDto;
import com.sequenceiq.it.cloudbreak.exception.TestFailException;
import com.sequenceiq.it.cloudbreak.testcase.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Value;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;

public class EnvironmentListTest extends AbstractIntegrationTest {

    @Inject
    private EnvironmentTestClient environmentTestClient;

    @Inject
    private CredentialTestClient credentialTestClient;

    private String credentialName;

    @Value("${perf.env.amount:500}")
    private int amountOfEnvironmentToCreate;

    @Override
    protected void setupTest(TestContext testContext) {
        createDefaultUser(testContext);
        credentialName = resourcePropertyProvider().getName();
        testContext.given(CredentialTestDto.class)
                .withName(credentialName)
                .when(credentialTestClient.create());
        createDefaultUser(testContext);
        initializeDefaultBlueprints(testContext);
        createEnvironments(testContext);
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK, invocationCount = 4, threadPoolSize = 10) // 1000, 10
    @Description(
            given = "there is a running environment service with a LOT OF created environment(s)",
            when = "the user lists the environment",
            then = "the response times should be fine")
    public void testListingEnvironments(MockedTestContext testContext) {
        testContext
                .given(EnvironmentTestDto.class)
                .when(environmentTestClient.list())
                .then(this::checkAllEnvListed);
    }

    private void createEnvironments(TestContext testContext) {
        getEnvironmentNames()
                .parallelStream()
                .forEach(envName -> testContext
                        .given(EnvironmentTestDto.class)
                        .withName(envName)
                        .withCredentialName(credentialName)
                        .when(environmentTestClient.create())
                        .await(EnvironmentStatus.AVAILABLE)
                        .validate());
    }

    private Set<String> getEnvironmentNames() {
        Set<String> names = new LinkedHashSet<>(amountOfEnvironmentToCreate);
        for (int i = 0; i < amountOfEnvironmentToCreate; i++) {
            names.add(resourcePropertyProvider().getEnvironmentName());
        }
        return names;
    }

    private EnvironmentTestDto checkAllEnvListed(TestContext testContext, EnvironmentTestDto environment,
                                                 EnvironmentClient environmentClient) {
        long result = environment.getResponseSimpleEnvSet().stream()
                .filter(env -> environment.getName().equals(env.getName()))
                .count();
        if (result != amountOfEnvironmentToCreate) {
            throw new TestFailException("We've listed a different number of environments!");
        }
        return environment;
    }

}
