package com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudStorageRequest {

    private S3Guard s3Guard;

    private List<StorageLocationRequest> locations = new ArrayList<>();

    private List<StorageIdentityRequest> identities = new ArrayList<>();

    public S3Guard getS3Guard() {
        return s3Guard;
    }

    public void setS3Guard(S3Guard s3Guard) {
        this.s3Guard = s3Guard;
    }

    public List<StorageLocationRequest> getLocations() {
        return locations;
    }

    public void setLocations(List<StorageLocationRequest> locations) {
        this.locations = locations;
    }

    public List<StorageIdentityRequest> getIdentities() {
        return identities;
    }

    public void setIdentities(List<StorageIdentityRequest> identities) {
        this.identities = identities;
    }
}
