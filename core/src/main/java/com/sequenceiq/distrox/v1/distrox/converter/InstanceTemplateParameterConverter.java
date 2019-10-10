package com.sequenceiq.distrox.v1.distrox.converter;

import static com.sequenceiq.cloudbreak.util.NullUtil.getIfNotNull;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.parameter.template.AwsEncryptionV4Parameters;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.parameter.template.AwsInstanceTemplateV4Parameters;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.parameter.template.AzureInstanceTemplateV4Parameters;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.parameter.template.GcpInstanceTemplateV4Parameters;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.parameter.template.OpenStackInstanceTemplateV4Parameters;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.parameter.template.YarnInstanceTemplateV4Parameters;
import com.sequenceiq.distrox.api.v1.distrox.model.instancegroup.template.AwsEncryptionV1Parameters;
import com.sequenceiq.distrox.api.v1.distrox.model.instancegroup.template.AwsInstanceTemplateV1Parameters;
import com.sequenceiq.distrox.api.v1.distrox.model.instancegroup.template.AzureInstanceTemplateV1Parameters;
import com.sequenceiq.distrox.api.v1.distrox.model.instancegroup.template.GcpInstanceTemplateV1Parameters;
import com.sequenceiq.distrox.api.v1.distrox.model.instancegroup.template.OpenstackInstanceTemplateV1Parameters;
import com.sequenceiq.distrox.api.v1.distrox.model.instancegroup.template.YarnInstanceTemplateV1Parameters;

@Component
public class InstanceTemplateParameterConverter {

    public AwsInstanceTemplateV4Parameters convert(AwsInstanceTemplateV1Parameters source) {
        AwsInstanceTemplateV4Parameters response = new AwsInstanceTemplateV4Parameters();
        response.setEncryption(getIfNotNull(source.getEncryption(), this::convert));
        response.setSpotPrice(source.getSpotPrice());
        return response;
    }

    private AwsEncryptionV4Parameters convert(AwsEncryptionV1Parameters source) {
        AwsEncryptionV4Parameters response = new AwsEncryptionV4Parameters();
        response.setKey(source.getKey());
        response.setType(source.getType());
        return response;
    }

    public AwsInstanceTemplateV1Parameters convert(AwsInstanceTemplateV4Parameters source) {
        AwsInstanceTemplateV1Parameters response = new AwsInstanceTemplateV1Parameters();
        response.setEncryption(getIfNotNull(source.getEncryption(), this::convert));
        response.setSpotPrice(source.getSpotPrice());
        return response;
    }

    private AwsEncryptionV1Parameters convert(AwsEncryptionV4Parameters source) {
        AwsEncryptionV1Parameters response = new AwsEncryptionV1Parameters();
        response.setKey(source.getKey());
        response.setType(source.getType());
        return response;
    }

    public GcpInstanceTemplateV4Parameters convert(GcpInstanceTemplateV1Parameters source) {
        GcpInstanceTemplateV4Parameters response = new GcpInstanceTemplateV4Parameters();
        return response;
    }

    public GcpInstanceTemplateV1Parameters convert(GcpInstanceTemplateV4Parameters source) {
        GcpInstanceTemplateV1Parameters response = new GcpInstanceTemplateV1Parameters();
        return response;
    }

    public OpenStackInstanceTemplateV4Parameters convert(OpenstackInstanceTemplateV1Parameters source) {
        OpenStackInstanceTemplateV4Parameters response = new OpenStackInstanceTemplateV4Parameters();
        return response;
    }

    public OpenstackInstanceTemplateV1Parameters convert(OpenStackInstanceTemplateV4Parameters source) {
        OpenstackInstanceTemplateV1Parameters response = new OpenstackInstanceTemplateV1Parameters();
        return response;
    }

    public AzureInstanceTemplateV4Parameters convert(AzureInstanceTemplateV1Parameters source) {
        AzureInstanceTemplateV4Parameters response = new AzureInstanceTemplateV4Parameters();
        response.setEncrypted(source.getEncrypted());
        response.setManagedDisk(source.getManagedDisk());
        response.setPrivateId(source.getPrivateId());
        return response;
    }

    public YarnInstanceTemplateV4Parameters convert(YarnInstanceTemplateV1Parameters source) {
        YarnInstanceTemplateV4Parameters response = new YarnInstanceTemplateV4Parameters();
        response.setCpus(source.getCpus());
        response.setMemory(source.getMemory());
        return response;
    }

    public AzureInstanceTemplateV1Parameters convert(AzureInstanceTemplateV4Parameters source) {
        AzureInstanceTemplateV1Parameters response = new AzureInstanceTemplateV1Parameters();
        response.setEncrypted(source.getEncrypted());
        response.setManagedDisk(source.getManagedDisk());
        response.setPrivateId(source.getPrivateId());
        return response;
    }

    public YarnInstanceTemplateV1Parameters convert(YarnInstanceTemplateV4Parameters source) {
        YarnInstanceTemplateV1Parameters response = new YarnInstanceTemplateV1Parameters();
        response.setCpus(source.getCpus());
        response.setMemory(source.getMemory());
        return response;
    }
}
