package com.sequenceiq.cloudbreak.cloud.openstack.nativ;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.NetworkConnector;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.Network;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Variant;
import com.sequenceiq.cloudbreak.cloud.model.network.CreatedCloudNetwork;
import com.sequenceiq.cloudbreak.cloud.model.network.NetworkCreationRequest;
import com.sequenceiq.cloudbreak.cloud.model.network.NetworkDeletionRequest;
import com.sequenceiq.cloudbreak.cloud.openstack.common.OpenStackConstants;

@Component
public class OpenStackNativeNetworkConnector implements NetworkConnector {

    @Override
    public CreatedCloudNetwork createNetworkWithSubnets(NetworkCreationRequest networkCreationRequest, String creatorUser) {
        return null;
    }

    @Override
    public void deleteNetworkWithSubnets(NetworkDeletionRequest networkDeletionRequest) {

    }

    @Override
    public String getNetworkCidr(Network network, CloudCredential credential) {
        return null;
    }

    @Override
    public Platform platform() {
        return OpenStackConstants.OPENSTACK_PLATFORM;
    }

    @Override
    public Variant variant() {
        return OpenStackConstants.OpenStackVariant.NATIVE.variant();
    }
}
