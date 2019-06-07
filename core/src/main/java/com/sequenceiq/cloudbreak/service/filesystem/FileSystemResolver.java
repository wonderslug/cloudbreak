package com.sequenceiq.cloudbreak.service.filesystem;

import java.io.IOException;

import javax.ws.rs.BadRequestException;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.StorageIdentityV4;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.parameter.storage.CloudStorageV4Parameters;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage.CloudStorageV4Request;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.common.type.filesystem.BaseFileSystem;

@Service
public class FileSystemResolver {

    private static final String NOT_SUPPORTED_FS_PROVIDED = "Unable to determine file system type, or unsupported file system type provided!";

    public CloudStorageV4Parameters propagateConfiguration(CloudStorageV4Request source) {
        CloudStorageV4Parameters cloudStorageParameters;
        StorageIdentityV4 identity = source.getIdentity();
        if (identity != null) {
            if (identity.getAdls() != null) {
                cloudStorageParameters = identity.getAdls();
            } else if (identity.getGcs() != null) {
                cloudStorageParameters = identity.getGcs();
            } else if (identity.getWasb() != null) {
                cloudStorageParameters = identity.getWasb();
            } else if (identity.getS3() != null) {
                cloudStorageParameters = identity.getS3();
            } else if (identity.getAdlsGen2() != null) {
                cloudStorageParameters = identity.getAdlsGen2();
            } else {
                throw new BadRequestException(NOT_SUPPORTED_FS_PROVIDED);
            }
        } else {
            throw new BadRequestException(NOT_SUPPORTED_FS_PROVIDED);
        }

        return cloudStorageParameters;
    }

    public BaseFileSystem propagateConfiguration(FileSystem source) {
        BaseFileSystem fileSystem;
        try {
            fileSystem = source.getConfigurations().get(BaseFileSystem.class);
        } catch (IOException e) {
            throw new BadRequestException(NOT_SUPPORTED_FS_PROVIDED);
        }
        return fileSystem;
    }

}
