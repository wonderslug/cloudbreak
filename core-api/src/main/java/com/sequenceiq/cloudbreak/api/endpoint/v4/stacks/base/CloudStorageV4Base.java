package com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sequenceiq.cloudbreak.api.endpoint.v4.JsonEntity;

import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CloudStorageV4Base implements JsonEntity {

    @Valid
    @ApiModelProperty
    private StorageIdentityV4 identity;

    public StorageIdentityV4 getIdentity() {
        return identity;
    }

    public void setIdentity(StorageIdentityV4 identity) {
        this.identity = identity;
    }
}
