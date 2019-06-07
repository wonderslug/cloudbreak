package com.sequenceiq.cloudbreak.converter.util;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.CloudStorageV4Request;

@Component
public class CloudStorageValidationUtil {

    public boolean isCloudStorageConfigured(CloudStorageV4Request cloudStorageRequest) {
        return cloudStorageRequest != null && cloudStorageRequest.getIdentity() != null
                && (cloudStorageRequest.getIdentity().getAdls() != null
                || cloudStorageRequest.getIdentity().getGcs() != null
                || cloudStorageRequest.getIdentity().getS3() != null
                || cloudStorageRequest.getIdentity().getWasb() != null
                || cloudStorageRequest.getIdentity().getAdlsGen2() != null);
    }
}
