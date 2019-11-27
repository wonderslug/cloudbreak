package com.sequenceiq.it.cloudbreak.action;

import java.util.Vector;

import com.sequenceiq.it.cloudbreak.MicroserviceClient;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.CloudbreakTestDto;

public class MeasuredAction<T extends CloudbreakTestDto, U extends MicroserviceClient> implements Action<T, U> {

    private final Vector<PerformanceIndicator> measurments = new Vector<>();

    private Action<T, U> action;

    @Override
    public T action(TestContext testContext, T testDto, U client) throws Exception {
        long start = System.currentTimeMillis();
        T result = action.action(testContext, testDto, client);
        long duration = System.currentTimeMillis() - start;
        measurments.add(new PerformanceIndicator(start, duration));
        return result;
    }

    public Vector<PerformanceIndicator> getMeasurments() {
        return measurments;
    }
}
