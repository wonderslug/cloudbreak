package com.sequenceiq.it.cloudbreak.context;

import com.sequenceiq.it.cloudbreak.MicroserviceClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.dto.CloudbreakTestDto;

public class MeasuredMockTestContext extends MockedTestContext{
    @Override
    protected <T extends CloudbreakTestDto, U extends MicroserviceClient> T doAction(T entity, Class<? extends MicroserviceClient> clientClass, Action<T, U> action, String who) throws Exception {
        return super.doAction(entity, clientClass, action, who);
    }
}
