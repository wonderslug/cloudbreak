package com.sequenceiq.cloudbreak.cloud.aws.view;

import java.util.Set;

import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.SpiFileSystem;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudS3View;

public class AwsInstanceProfileView {

    private final Set<SpiFileSystem> cloudFileSystems;

    public AwsInstanceProfileView(CloudStack stack) {
        cloudFileSystems = stack.getFileSystems();
    }

    public boolean isInstanceProfileAvailable() {
        return !cloudFileSystems.isEmpty() && (cloudFileSystems.iterator().next().getCloudFileSystem() instanceof CloudS3View);
    }

    public String getInstanceProfile() {
        // TODO: this is only temporary!!!
        return ((CloudS3View) cloudFileSystems.iterator().next().getCloudFileSystem()).getInstanceProfile();
    }

}
