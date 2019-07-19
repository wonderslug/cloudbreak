package com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageLocationRequest {

    private CloudStorageCdpService type;

    private String value;

    public CloudStorageCdpService getType() {
        return type;
    }

    public void setType(CloudStorageCdpService type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
