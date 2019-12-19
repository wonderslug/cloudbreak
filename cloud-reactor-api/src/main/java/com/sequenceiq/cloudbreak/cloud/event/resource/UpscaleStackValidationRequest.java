package com.sequenceiq.cloudbreak.cloud.event.resource;

import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;

public class UpscaleStackValidationRequest<T> extends CloudStackRequest<T> {

    private final Integer newInstanceCount;

    private final String instanceType;

    public UpscaleStackValidationRequest(CloudContext cloudContext, CloudCredential cloudCredential, CloudStack stack, Integer newInstanceCount, String instanceType) {
        super(cloudContext, cloudCredential, stack);
        this.newInstanceCount = newInstanceCount;
        this.instanceType = instanceType;
    }

    public Integer getNewInstanceCount() {
        return newInstanceCount;
    }

    public String getInstanceType() {
        return instanceType;
    }
}
