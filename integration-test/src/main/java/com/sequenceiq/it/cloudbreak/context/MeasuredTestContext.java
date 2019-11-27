package com.sequenceiq.it.cloudbreak.context;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.Status;
import com.sequenceiq.environment.api.v1.environment.model.response.EnvironmentStatus;
import com.sequenceiq.it.cloudbreak.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.MicroserviceClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.actor.Actor;
import com.sequenceiq.it.cloudbreak.assertion.Assertion;
import com.sequenceiq.it.cloudbreak.cloud.v4.CloudProviderProxy;
import com.sequenceiq.it.cloudbreak.dto.CloudbreakTestDto;
import com.sequenceiq.it.cloudbreak.dto.environment.EnvironmentTestDto;
import com.sequenceiq.it.cloudbreak.dto.freeipa.FreeIPATestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxInternalTestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxRepairTestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxTestDto;
import com.sequenceiq.it.cloudbreak.finder.Attribute;
import com.sequenceiq.it.cloudbreak.finder.Finder;
import com.sequenceiq.sdx.api.model.SdxClusterStatusResponse;

public class MeasuredTestContext extends TestContext {
    private TestContext wrappedTestContext;

    @Override
    public <T extends CloudbreakTestDto> T then(T entity, Class<? extends MicroserviceClient> clientClass, Assertion<T, ? extends MicroserviceClient> assertion, RunningParameter runningParameter) {
        return wrappedTestContext.then(entity, clientClass, assertion, runningParameter);
    }

    @Override
    public TestContext as(Actor actor) {
        return wrappedTestContext.as(actor);
    }

    @Override
    public Map<String, CloudbreakTestDto> getResources() {
        return wrappedTestContext.getResources();
    }

    @Override
    public Map<String, Map<Class<? extends MicroserviceClient>, MicroserviceClient>> getClients() {
        return wrappedTestContext.getClients();
    }

    @Override
    public Map<String, Exception> getExceptionMap() {
        return wrappedTestContext.getExceptionMap();
    }

    @Override
    public void setShutdown(boolean shutdown) {
        wrappedTestContext.setShutdown(shutdown);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return wrappedTestContext.getApplicationContext();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        wrappedTestContext.setApplicationContext(applicationContext);
    }

    @Override
    protected void checkShutdown() {
        wrappedTestContext.checkShutdown();
    }

    @Override
    protected String getDefaultUser() {
        return wrappedTestContext.getDefaultUser();
    }

    @Override
    public <O extends CloudbreakTestDto> O init(Class<O> clss) {
        return wrappedTestContext.init(clss);
    }

    @Override
    public <O extends CloudbreakTestDto> O given(String key, Class<O> clss) {
        return wrappedTestContext.given(key, clss);
    }

    @Override
    public Map<String, String> getStatuses() {
        return wrappedTestContext.getStatuses();
    }

    @Override
    public Map<String, Exception> getErrors() {
        return wrappedTestContext.getErrors();
    }

    @Override
    public <T extends CloudbreakTestDto> T get(String key) {
        return wrappedTestContext.get(key);
    }

    @Override
    public <O> O getSelected(String key) {
        return wrappedTestContext.getSelected(key);
    }

    @Override
    public <O> O getRequiredSelected(String key) {
        return wrappedTestContext.getRequiredSelected(key);
    }

    @Override
    public <O, T extends CloudbreakTestDto> T select(T entity, Attribute<T, O> attribute, Finder<O> finder, RunningParameter runningParameter) {
        return wrappedTestContext.select(entity, attribute, finder, runningParameter);
    }

    @Override
    public <O, T extends CloudbreakTestDto> T capture(T entity, Attribute<T, O> attribute, RunningParameter runningParameter) {
        return wrappedTestContext.capture(entity, attribute, runningParameter);
    }

    @Override
    public <O, T extends CloudbreakTestDto> T verify(T entity, Attribute<T, O> attribute, RunningParameter runningParameter) {
        return wrappedTestContext.verify(entity, attribute, runningParameter);
    }

    @Override
    public CloudbreakClient getCloudbreakClient(String who) {
        return wrappedTestContext.getCloudbreakClient(who);
    }

    @Override
    public <U extends MicroserviceClient> U getMicroserviceClient(Class<? extends MicroserviceClient> msClientClass, String who) {
        return wrappedTestContext.getMicroserviceClient(msClientClass, who);
    }

    @Override
    public CloudbreakClient getCloudbreakClient() {
        return wrappedTestContext.getCloudbreakClient();
    }

    @Override
    public <U extends MicroserviceClient> U getMicroserviceClient(Class<? extends MicroserviceClient> msClientClass) {
        return wrappedTestContext.getMicroserviceClient(msClientClass);
    }

    @Override
    public <T extends CloudbreakTestDto> T await(T entity, Map<String, Status> desiredStatuses, RunningParameter runningParameter, long pollingInterval) {
        return wrappedTestContext.await(entity, desiredStatuses, runningParameter, pollingInterval);
    }

    @Override
    public EnvironmentTestDto await(EnvironmentTestDto entity, EnvironmentStatus desiredStatuses, RunningParameter runningParameter, long pollingInterval) {
        return wrappedTestContext.await(entity, desiredStatuses, runningParameter, pollingInterval);
    }

    @Override
    public SdxTestDto await(SdxTestDto entity, SdxClusterStatusResponse desiredStatuses, RunningParameter runningParameter, long pollingInterval) {
        return wrappedTestContext.await(entity, desiredStatuses, runningParameter, pollingInterval);
    }

    @Override
    public SdxInternalTestDto await(SdxInternalTestDto entity, SdxClusterStatusResponse desiredStatuses, RunningParameter runningParameter, long pollingInterval) {
        return wrappedTestContext.await(entity, desiredStatuses, runningParameter, pollingInterval);
    }

    @Override
    public SdxRepairTestDto await(SdxRepairTestDto entity, SdxClusterStatusResponse desiredStatuses, RunningParameter runningParameter, long pollingInterval) {
        return wrappedTestContext.await(entity, desiredStatuses, runningParameter, pollingInterval);
    }

    @Override
    public FreeIPATestDto await(FreeIPATestDto entity, com.sequenceiq.freeipa.api.v1.freeipa.stack.model.common.Status desiredStatuses, RunningParameter runningParameter, long pollingInterval) {
        return wrappedTestContext.await(entity, desiredStatuses, runningParameter, pollingInterval);
    }

    @Override
    public <E extends Exception, T extends CloudbreakTestDto> T expect(T entity, Class<E> expectedException, RunningParameter runningParameter) {
        return wrappedTestContext.expect(entity, expectedException, runningParameter);
    }

    @Override
    public void handleExceptionsDuringTest(boolean silently) {
        wrappedTestContext.handleExceptionsDuringTest(silently);
    }

    @Override
    public void cleanupTestContext() {
        wrappedTestContext.cleanupTestContext();
    }

    @Override
    public void shutdown() {
        wrappedTestContext.shutdown();
    }

    @Override
    public CloudProviderProxy getCloudProvider() {
        return super.getCloudProvider();
    }

    @Override
    protected <T extends CloudbreakTestDto, U extends MicroserviceClient> T doAction(T entity, Class<? extends MicroserviceClient> clientClass, Action<T, U> action, String who) throws Exception {
        T result = wrappedTestContext.doAction(entity, clientClass, action, who);
        //TODO: do measurements;
        return result;
    }

    public static MeasuredTestContext createMeasuredTestContext(TestContext testContext) {
        return new MeasuredTestContext(testContext);
    }

    private MeasuredTestContext(TestContext wrappedTestContext) {
        this.wrappedTestContext = wrappedTestContext;
    }
}
