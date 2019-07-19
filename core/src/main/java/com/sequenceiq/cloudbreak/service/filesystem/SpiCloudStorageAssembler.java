package com.sequenceiq.cloudbreak.service.filesystem;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.CloudStorageCdpService;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.CloudStorageRequest;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.StorageLocationRequest;
import com.sequenceiq.cloudbreak.cloud.model.SpiFileSystem;
import com.sequenceiq.cloudbreak.cmtemplate.cloudstorage.CmCloudStorageConfigDetails;
import com.sequenceiq.cloudbreak.template.filesystem.query.ConfigQueryEntries;

@Service
public class SpiCloudStorageAssembler {

    @Inject
    private CmCloudStorageConfigDetails cmCloudStorageConfigDetails;

    public Set<SpiFileSystem> assembleSpiFileSystems(CloudStorageRequest cloudStorageRequest) {
        List<StorageLocationRequest> locations = cloudStorageRequest.getLocations();
        CloudStorageCdpService rangerAdmin = CloudStorageCdpService.RANGER_ADMIN;
        ConfigQueryEntries configQueryEntries = cmCloudStorageConfigDetails.getConfigQueryEntries();
        // TODO finish this somehow
        return Collections.emptySet();
    }

}
