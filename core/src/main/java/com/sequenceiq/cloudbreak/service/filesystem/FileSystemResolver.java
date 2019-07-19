package com.sequenceiq.cloudbreak.service.filesystem;

import java.io.IOException;

import javax.ws.rs.BadRequestException;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.common.api.cloudstorage.CloudStorageV1Base;
import com.sequenceiq.common.api.filesystem.BaseFileSystem;
import com.sequenceiq.common.model.FileSystemAwareCloudStorage;

@Service
public class FileSystemResolver {

    private static final String NOT_SUPPORTED_FS_PROVIDED = "Unable to determine file system type, or unsupported file system type provided!";

    public FileSystemAwareCloudStorage resolveFileSystem(CloudStorageV1Base source) {
        FileSystemAwareCloudStorage cloudStorageParameters;
        if (source.getAdls() != null) {
            cloudStorageParameters = source.getAdls();
        } else if (source.getGcs() != null) {
            cloudStorageParameters = source.getGcs();
        } else if (source.getWasb() != null) {
            cloudStorageParameters = source.getWasb();
        } else if (source.getS3() != null) {
            cloudStorageParameters = source.getS3();
        } else if (source.getAdlsGen2() != null) {
            cloudStorageParameters = source.getAdlsGen2();
        } else {
            throw new BadRequestException(NOT_SUPPORTED_FS_PROVIDED);
        }
        return cloudStorageParameters;
    }

    public BaseFileSystem resolveFileSystem(FileSystem source) {
        BaseFileSystem fileSystem;
        try {
            fileSystem = source.getConfigurations().get(BaseFileSystem.class);
        } catch (IOException e) {
            throw new BadRequestException(NOT_SUPPORTED_FS_PROVIDED);
        }
        return fileSystem;
    }

}
