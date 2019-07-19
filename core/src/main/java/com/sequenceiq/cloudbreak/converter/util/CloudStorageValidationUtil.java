package com.sequenceiq.cloudbreak.converter.util;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.CloudStorageRequest;

@Component
public class CloudStorageValidationUtil {

    public boolean isCloudStorageConfigured(CloudStorageRequest cloudStorageRequest) {
        return CollectionUtils.isNotEmpty(cloudStorageRequest.getLocations());
    }
}
