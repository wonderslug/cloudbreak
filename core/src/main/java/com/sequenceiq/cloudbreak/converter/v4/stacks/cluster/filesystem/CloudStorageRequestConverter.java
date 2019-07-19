package com.sequenceiq.cloudbreak.converter.v4.stacks.cluster.filesystem;

import static com.sequenceiq.cloudbreak.common.type.APIResourceType.FILESYSTEM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.CloudStorageCdpIdentity;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.CloudStorageRequest;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.StorageIdentityRequest;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.StorageLocationRequest;
import com.sequenceiq.cloudbreak.cmtemplate.cloudstorage.CmCloudStorageConfigDetails;
import com.sequenceiq.cloudbreak.common.converter.MissingResourceNameGenerator;
import com.sequenceiq.cloudbreak.common.json.Json;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.StorageLocation;
import com.sequenceiq.cloudbreak.domain.StorageLocations;
import com.sequenceiq.cloudbreak.exception.BadRequestException;
import com.sequenceiq.cloudbreak.service.filesystem.FileSystemResolver;
import com.sequenceiq.cloudbreak.template.filesystem.query.ConfigQueryEntries;
import com.sequenceiq.common.api.cloudstorage.AdlsCloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.AdlsGen2CloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.GcsCloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.S3CloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.WasbCloudStorageV1Parameters;
import com.sequenceiq.common.api.filesystem.AdlsFileSystem;
import com.sequenceiq.common.api.filesystem.AdlsGen2FileSystem;
import com.sequenceiq.common.api.filesystem.BaseFileSystem;
import com.sequenceiq.common.api.filesystem.GcsFileSystem;
import com.sequenceiq.common.api.filesystem.S3FileSystem;
import com.sequenceiq.common.api.filesystem.WasbFileSystem;
import com.sequenceiq.common.model.FileSystemAwareCloudStorage;
import com.sequenceiq.common.model.FileSystemType;

@Component
public class CloudStorageRequestConverter {

    private final MissingResourceNameGenerator nameGenerator;

    private final FileSystemResolver fileSystemResolver;

    private final CmCloudStorageConfigDetails cmCloudStorageConfigDetails;

    private final Map<FileSystemType, Function<FileSystemAwareCloudStorage, ? extends BaseFileSystem>> fileSystemConverters = new HashMap<>();

    public CloudStorageRequestConverter(MissingResourceNameGenerator nameGenerator, FileSystemResolver fileSystemResolver,
            CmCloudStorageConfigDetails cmCloudStorageConfigDetails) {
        this.nameGenerator = nameGenerator;
        this.fileSystemResolver = fileSystemResolver;
        this.cmCloudStorageConfigDetails = cmCloudStorageConfigDetails;
        fileSystemConverters.put(FileSystemType.S3, this::convertS3);
        fileSystemConverters.put(FileSystemType.ADLS_GEN_2, this::convertAdlsGen2);
        fileSystemConverters.put(FileSystemType.WASB, this::convertWasb);
        fileSystemConverters.put(FileSystemType.ADLS, this::convertAdls);
        fileSystemConverters.put(FileSystemType.GCS, this::convertGcs);
    }

    public Set<FileSystem> convert(CloudStorageRequest cloudStorage) {
        Optional<StorageIdentityRequest> idbrokerOpt = cloudStorage.getIdentities().stream()
                .filter(identity -> identity.getType().equals(CloudStorageCdpIdentity.IDBROKER)).findFirst();
        StorageIdentityRequest log = cloudStorage.getIdentities().stream()
                .filter(identity -> identity.getType().equals(CloudStorageCdpIdentity.LOG)).findFirst()
                .orElseThrow(() -> new BadRequestException("LOG identity must be present in the CloudStorageRequest"));

        return cloudStorage.getLocations().stream().map(location -> {
            FileSystemAwareCloudStorage fileSystemAwareCloudStorage = fileSystemResolver.resolveFileSystem(log);

            FileSystem fileSystem = new FileSystem();
            fileSystem.setName(nameGenerator.generateName(FILESYSTEM));
            fileSystem.setType(fileSystemAwareCloudStorage.getType());

            Set<StorageLocation> storageLocationSet = storageLocationRequestsToStorageLocations(cloudStorage.getLocations());
            StorageLocations storageLocations = new StorageLocations();
            storageLocations.setLocations(storageLocationSet);
            fileSystem.setLocations(storageLocations);

            BaseFileSystem baseFileSystem = fileSystemConverters.computeIfAbsent(fileSystemAwareCloudStorage.getType(), a -> {
                throw new BadRequestException("");
            }).apply(fileSystemAwareCloudStorage);
            fileSystem.setConfigurations(new Json(baseFileSystem));

            return fileSystem;

        }).collect(Collectors.toSet());
    }

    public CloudStorageRequest convert(Set<FileSystem> fileSystems) {
        CloudStorageRequest result = new CloudStorageRequest();
        List<StorageIdentityRequest> storageIdentityRequests = new ArrayList<>();
        List<StorageLocationRequest> storageLocationRequests = new ArrayList<>();
        result.setIdentities(storageIdentityRequests);
        result.setLocations(storageLocationRequests);
        fileSystems.forEach(fileSystem -> {
            StorageLocations storageLocations = fileSystem.getLocationsObject();
            // TODO: possible bug? no idea!
            storageLocationRequests.addAll(storageLocationsToStorageLocationRequests(storageLocations.getLocations()));
            // TODO: use this to extract stuff for response:
            Json configurations = fileSystem.getConfigurations();
        });

        // TODO: create StorageIdentityRequests (IDBROKER and LOG)

        return result;
    }

    private S3FileSystem convertS3(FileSystemAwareCloudStorage source) {
        S3CloudStorageV1Parameters s3 = (S3CloudStorageV1Parameters) source;
        S3FileSystem fileSystem = new S3FileSystem();
        fileSystem.setInstanceProfile(s3.getInstanceProfile());
        return fileSystem;
    }

    private AdlsGen2FileSystem convertAdlsGen2(FileSystemAwareCloudStorage source) {
        AdlsGen2CloudStorageV1Parameters adlsGen2 = (AdlsGen2CloudStorageV1Parameters) source;
        AdlsGen2FileSystem fileSystem = new AdlsGen2FileSystem();
        fileSystem.setAccountName(adlsGen2.getAccountName());
        fileSystem.setAccountKey(adlsGen2.getAccountKey());
        fileSystem.setSecure(adlsGen2.isSecure());
        return fileSystem;
    }

    private WasbFileSystem convertWasb(FileSystemAwareCloudStorage source) {
        WasbCloudStorageV1Parameters wasb = (WasbCloudStorageV1Parameters) source;
        WasbFileSystem fileSystem = new WasbFileSystem();
        fileSystem.setAccountKey(wasb.getAccountKey());
        fileSystem.setAccountName(wasb.getAccountName());
        fileSystem.setSecure(wasb.isSecure());
        return fileSystem;
    }

    private AdlsFileSystem convertAdls(FileSystemAwareCloudStorage source) {
        AdlsCloudStorageV1Parameters adls = (AdlsCloudStorageV1Parameters) source;
        AdlsFileSystem fileSystem = new AdlsFileSystem();
        fileSystem.setClientId(adls.getClientId());
        fileSystem.setAccountName(adls.getAccountName());
        fileSystem.setCredential(adls.getCredential());
        fileSystem.setTenantId(adls.getTenantId());
        return fileSystem;
    }

    private GcsFileSystem convertGcs(FileSystemAwareCloudStorage source) {
        GcsCloudStorageV1Parameters gcs = (GcsCloudStorageV1Parameters) source;
        GcsFileSystem fileSystem = new GcsFileSystem();
        fileSystem.setServiceAccountEmail(gcs.getServiceAccountEmail());
        return fileSystem;
    }

    private Set<StorageLocation> storageLocationRequestsToStorageLocations(List<StorageLocationRequest> storageLocationRequests) {
        Set<StorageLocation> locations = new HashSet<>();
        if (CollectionUtils.isNotEmpty(storageLocationRequests)) {
            ConfigQueryEntries configQueryEntries = cmCloudStorageConfigDetails.getConfigQueryEntries();
            for (StorageLocationRequest request : storageLocationRequests) {
                StorageLocation storageLocation = new StorageLocation();
                // TODO: get the config file and property names somehow
//                storageLocation.setConfigFile(request.getPropertyFile());
//                storageLocation.setProperty(request.getPropertyName());
                storageLocation.setValue(request.getValue());
            }
        }
        return locations;
    }

    private List<StorageLocationRequest> storageLocationsToStorageLocationRequests(Set<StorageLocation> storageLocations) {
        List<StorageLocationRequest> locations = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(storageLocations)) {
            ConfigQueryEntries configQueryEntries = cmCloudStorageConfigDetails.getConfigQueryEntries();
            for (StorageLocation storageLocation : storageLocations) {
                StorageLocationRequest storageLocationRequest = new StorageLocationRequest();
                // TODO: get the CloudStorageCdpService somehow
//                storageLocationRequest.setType();
                storageLocationRequest.setValue(storageLocation.getValue());
            }
        }
        return locations;
    }
}
