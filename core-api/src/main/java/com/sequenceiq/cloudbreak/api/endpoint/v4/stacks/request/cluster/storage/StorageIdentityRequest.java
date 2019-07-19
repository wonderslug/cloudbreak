package com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sequenceiq.common.api.cloudstorage.CloudStorageV1Base;

import io.swagger.annotations.ApiModel;

@ApiModel
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageIdentityRequest extends CloudStorageV1Base {

    private CloudStorageCdpIdentity type;

    public CloudStorageCdpIdentity getType() {
        return type;
    }

    public void setType(CloudStorageCdpIdentity type) {
        this.type = type;
    }
}
