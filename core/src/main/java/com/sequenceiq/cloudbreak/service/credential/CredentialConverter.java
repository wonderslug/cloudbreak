package com.sequenceiq.cloudbreak.service.credential;

import static com.sequenceiq.cloudbreak.util.NullUtil.ifNotNullF;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.common.json.Json;
import com.sequenceiq.cloudbreak.dto.credential.Credential;
import com.sequenceiq.cloudbreak.dto.credential.aws.AwsCredentialAttributes;
import com.sequenceiq.cloudbreak.dto.credential.aws.AwsKeyBasedAttributes;
import com.sequenceiq.cloudbreak.dto.credential.aws.AwsRoleBasedAttributes;
import com.sequenceiq.cloudbreak.dto.credential.azure.AzureCredentialAttributes;
import com.sequenceiq.cloudbreak.dto.credential.azure.AzureRoleBasedAttributes;
import com.sequenceiq.cloudbreak.service.secret.service.SecretService;
import com.sequenceiq.environment.api.v1.credential.model.parameters.aws.AwsCredentialParameters;
import com.sequenceiq.environment.api.v1.credential.model.parameters.aws.KeyBasedParameters;
import com.sequenceiq.environment.api.v1.credential.model.parameters.aws.RoleBasedParameters;
import com.sequenceiq.environment.api.v1.credential.model.parameters.azure.AzureCredentialResponseParameters;
import com.sequenceiq.environment.api.v1.credential.model.parameters.azure.RoleBasedResponse;
import com.sequenceiq.environment.api.v1.credential.model.response.CredentialResponse;

@Component
public class CredentialConverter {

    @Inject
    private SecretService secretService;

    public Set<Credential> convert(Collection<CredentialResponse> sources) {
        return sources.stream().map(this::convert).collect(Collectors.toSet());
    }

    public Credential convert(CredentialResponse source) {
        return Credential.builder()
                .aws(ifNotNullF(source.getAws(), this::aws))
                .azure(ifNotNullF(source.getAzure(), this::azure))
                .crn(source.getResourceCrn())
                .description(source.getDescription())
                .name(source.getName())
                .attributes(new Json(secretService.getByResponse(source.getAttributes())))
                .cloudPlatform(source.getCloudPlatform())
                .build();
    }

    private AwsCredentialAttributes aws(AwsCredentialParameters aws) {
        return AwsCredentialAttributes.builder()
                .govCloud(aws.getGovCloud())
                .keyBased(ifNotNullF(aws.getKeyBased(), this::keyBased))
                .roleBased(ifNotNullF(aws.getRoleBased(), this::roleBased))
                .build();
    }

    private AzureCredentialAttributes azure(AzureCredentialResponseParameters azure) {
        return AzureCredentialAttributes.builder()
                .accessKey(azure.getAccessKey())
                .roleBased(ifNotNullF(azure.getRoleBased(), this::roleBased))
                .subscriptionId(azure.getSubscriptionId())
                .tenantId(azure.getTenantId())
                .build();
    }

    private AzureRoleBasedAttributes roleBased(RoleBasedResponse roleBased) {
        return AzureRoleBasedAttributes.builder()
                .appObjectId(roleBased.getAppObjectId())
                .codeGrantFlow(roleBased.getCodeGrantFlow())
                .deploymentAddress(roleBased.getDeploymentAddress())
                .roleName(roleBased.getRoleName())
                .spDisplayName(roleBased.getSpDisplayName())
                .build();
    }

    private AwsRoleBasedAttributes roleBased(RoleBasedParameters roleBased) {
        return AwsRoleBasedAttributes.builder()
                .roleArn(roleBased.getRoleArn())
                .build();
    }

    private AwsKeyBasedAttributes keyBased(KeyBasedParameters keyBased) {
        return AwsKeyBasedAttributes.builder()
                .accessKey(keyBased.getAccessKey())
                .secretKey(keyBased.getSecretKey())
                .build();
    }
}
