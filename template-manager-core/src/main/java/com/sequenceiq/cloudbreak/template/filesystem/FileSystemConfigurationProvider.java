package com.sequenceiq.cloudbreak.template.filesystem;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.StackV4Request;
import com.sequenceiq.cloudbreak.common.json.Json;
import com.sequenceiq.cloudbreak.common.type.CloudConstants;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.Resource;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.common.api.type.ResourceType;

@Service
public class FileSystemConfigurationProvider {

    @Inject
    private AzureFileSystemConfigProvider azureFileSystemConfigProvider;

    @Inject
    private FileSystemConfigurationsViewProvider fileSystemConfigurationsViewProvider;

    public Set<BaseFileSystemConfigurationsView> fileSystemConfigurations(Set<FileSystem> fileSystems, Stack stack, Json attributes) {
        return fileSystems.stream().map(fileSystem -> fileSystemConfiguration(fileSystem, stack, attributes)).collect(Collectors.toSet());
    }

    public BaseFileSystemConfigurationsView fileSystemConfiguration(FileSystem fileSystem, Stack stack, Json credentialAttributes) {
        Optional<Resource> resource = Optional.empty();
        if (CloudConstants.AZURE.equals(stack.getPlatformVariant())) {
            resource = Optional.of(stack.getResourceByType(ResourceType.ARM_TEMPLATE));
        }
        return fileSystemConfiguration(fileSystem, stack.getId(), stack.getUuid(), credentialAttributes, stack.getPlatformVariant(), resource);
    }


    public Set<BaseFileSystemConfigurationsView> fileSystemConfigurations(Set<FileSystem> fileSystems, StackV4Request stack, Json attributes) {
        return fileSystems.stream().map(fileSystem -> fileSystemConfiguration(fileSystem, stack, attributes)).collect(Collectors.toSet());
    }

    public BaseFileSystemConfigurationsView fileSystemConfiguration(FileSystem fileSystem, StackV4Request request, Json credentialAttributes) {
        Resource resource = new Resource(ResourceType.ARM_TEMPLATE, request.getName(), null);
        return fileSystemConfiguration(fileSystem, 0L, "fake-uuid", credentialAttributes, request.getCloudPlatform().name(), Optional.of(resource));
    }

    private BaseFileSystemConfigurationsView fileSystemConfiguration(FileSystem fileSystem, Long stackId, String uuid, Json credentialAttributes,
            String platformVariant, Optional<Resource> resource) {
        try {
            BaseFileSystemConfigurationsView fileSystemConfiguration = null;
            if (fileSystem != null) {
                fileSystemConfiguration = fileSystemConfigurationsViewProvider.propagateConfigurationsView(fileSystem);
                fileSystemConfiguration.setStorageContainer("cloudbreak" + stackId);
                if (CloudConstants.AZURE.equals(platformVariant) && credentialAttributes != null) {
                    fileSystemConfiguration = azureFileSystemConfigProvider.decorateFileSystemConfiguration(uuid, credentialAttributes,
                            resource.orElse(null), fileSystemConfiguration);
                }
            }
            return fileSystemConfiguration;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
