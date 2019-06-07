package com.sequenceiq.cloudbreak.converter.spi;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.CloudStorageV4Request;
import com.sequenceiq.cloudbreak.cloud.model.SpiFileSystem;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudAdlsGen2View;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudAdlsView;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudFileSystemView;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudGcsView;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudS3View;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudWasbView;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.common.type.filesystem.FileSystemType;

@Component
public class CloudStorageV4RequestToSpiFileSystemConverter extends AbstractConversionServiceAwareConverter<CloudStorageV4Request, SpiFileSystem> {

    @Override
    public SpiFileSystem convert(CloudStorageV4Request source) {
        CloudFileSystemView baseFileSystem = null;
        FileSystemType type = null;
        if(source.getIdentity() != null) {
            if (source.getIdentity().getAdls() != null) {
                baseFileSystem = getConversionService().convert(source.getIdentity().getAdls(), CloudAdlsView.class);
                type = FileSystemType.ADLS;
            } else if (source.getIdentity().getGcs() != null) {
                baseFileSystem = getConversionService().convert(source.getIdentity().getGcs(), CloudGcsView.class);
                type = FileSystemType.GCS;
            } else if (source.getIdentity().getS3() != null) {
                baseFileSystem = getConversionService().convert(source.getIdentity().getS3(), CloudS3View.class);
                type = FileSystemType.S3;
            } else if (source.getIdentity().getWasb() != null) {
                baseFileSystem = getConversionService().convert(source.getIdentity().getWasb(), CloudWasbView.class);
                type = FileSystemType.WASB;
            } else if (source.getIdentity().getAdlsGen2() != null) {
                baseFileSystem = getConversionService().convert(source.getIdentity().getAdlsGen2(), CloudAdlsGen2View.class);
                type = FileSystemType.ADLS_GEN_2;
            }
        }
        return new SpiFileSystem("", type, baseFileSystem);
    }
}
