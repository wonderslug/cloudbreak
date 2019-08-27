package com.sequenceiq.environment.network.v1.converter;

import static com.sequenceiq.environment.CloudPlatform.OPENSTACK;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.model.network.CreatedCloudNetwork;
import com.sequenceiq.environment.CloudPlatform;
import com.sequenceiq.environment.network.dao.domain.BaseNetwork;
import com.sequenceiq.environment.network.dao.domain.OpenstackNetwork;
import com.sequenceiq.environment.network.dao.domain.RegistrationType;
import com.sequenceiq.environment.network.dto.NetworkDto;
import com.sequenceiq.environment.network.dto.OpenstackParams;

@Component
public class OpenstackEnvironmentNetworkConverter extends EnvironmentBaseNetworkConverter {

    @Override
    BaseNetwork createProviderSpecificNetwork(NetworkDto network) {
        OpenstackNetwork openstackNetwork = new OpenstackNetwork();
        return openstackNetwork;
    }

    @Override
    public BaseNetwork setProviderSpecificNetwork(BaseNetwork baseNetwork, CreatedCloudNetwork createdCloudNetwork) {
        OpenstackNetwork openstackNetwork = (OpenstackNetwork) baseNetwork;
        return openstackNetwork;
    }

    @Override
    NetworkDto setProviderSpecificFields(NetworkDto.Builder builder, BaseNetwork network) {
        OpenstackNetwork openstackNetwork = (OpenstackNetwork) network;
        return builder.withOpenstack(OpenstackParams.OpenstackParamsBuilder.anOpenstackParams().build()).build();
    }

    @Override
    void setRegistrationType(BaseNetwork result, NetworkDto networkDto) {
        if (isExistingNetworkSpecified(networkDto)) {
            result.setRegistrationType(RegistrationType.EXISTING);
        } else {
            result.setRegistrationType(RegistrationType.CREATE_NEW);
        }
    }

    private boolean isExistingNetworkSpecified(NetworkDto networkDto) {
        return networkDto.getOpenstack() != null && networkDto.getOpenstack().getVpcId() != null;
    }

    @Override
    public CloudPlatform getCloudPlatform() {
        return OPENSTACK;
    }

    @Override
    public boolean hasExistingNetwork(BaseNetwork baseNetwork) {
        return Optional.ofNullable((OpenstackNetwork) baseNetwork).map(OpenstackNetwork::getNetworkId).isPresent();
    }
}
