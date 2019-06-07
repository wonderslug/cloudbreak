package com.sequenceiq.cloudbreak.converter.v4.stacks.cluster.filesystem;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.StorageLocationV4;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.StorageLocation;

@Component
public class StorageLocationV4ToStorageLocationConverter extends AbstractConversionServiceAwareConverter<StorageLocationV4, StorageLocation> {

    @Override
    public StorageLocation convert(StorageLocationV4 source) {
        StorageLocation storageLocation = new StorageLocation();
        storageLocation.setConfigFile(source.getPropertyFile());
        storageLocation.setProperty(source.getPropertyName());
        storageLocation.setValue(source.getValue());
        return storageLocation;
    }
}
