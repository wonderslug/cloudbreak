package com.sequenceiq.cloudbreak.cloud.event.platform;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.ExtendedCloudCredential;

@Component
public class CloudReactorRequestProvider {
    public GetPlatformRegionsRequestV2 getPlatformRegionsRequestV2(CloudCredential cloudCredential,
            ExtendedCloudCredential extendedCloudCredential, String variant, String region,
            Map<String, String> filters) {
        return new GetPlatformRegionsRequestV2(cloudCredential, extendedCloudCredential, variant, region, filters);
    }
}
