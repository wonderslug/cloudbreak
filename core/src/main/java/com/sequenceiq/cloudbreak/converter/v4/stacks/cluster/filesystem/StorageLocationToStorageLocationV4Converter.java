package com.sequenceiq.cloudbreak.converter.v4.stacks.cluster.filesystem;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.StorageLocationV4;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.StorageLocation;

@Component
public class StorageLocationToStorageLocationV4Converter extends AbstractConversionServiceAwareConverter<StorageLocation, StorageLocationV4> {

    @Override
    public StorageLocationV4 convert(StorageLocation source) {
        StorageLocationV4 storageLocation = new StorageLocationV4();
        storageLocation.setPropertyFile(source.getConfigFile());
        storageLocation.setPropertyName(source.getProperty());
        storageLocation.setValue(source.getValue());
        return storageLocation;
    }
}
