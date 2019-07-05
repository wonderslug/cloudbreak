package com.sequenceiq.environment.credential.validation;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.common.json.Json;
import com.sequenceiq.cloudbreak.common.json.JsonUtil;
import com.sequenceiq.cloudbreak.util.ValidationResult;
import com.sequenceiq.environment.CloudPlatform;
import com.sequenceiq.environment.credential.domain.Credential;
import com.sequenceiq.environment.credential.validation.definition.CredentialDefinitionService;

@Component
public class CredentialValidator {

    @Value("${environment.enabledplatforms}")
    private Set<String> enabledPlatforms;

    @Inject
    private CredentialDefinitionService credentialDefinitionService;

    public void validateParameters(Platform platform, Json json) {
        credentialDefinitionService.checkProperties(platform, json);
    }

    public void validateCredentialCloudPlatform(String cloudPlatform) {
        if (!enabledPlatforms.contains(cloudPlatform)) {
            throw new BadRequestException(String.format("There is no such cloud platform as '%s'", cloudPlatform));
        }
    }

    public ValidationResult validateCredentialChange(Credential original, Credential newCred) {
        try {
            ValidationResult.ValidationResultBuilder resultBuilder = new ValidationResult.ValidationResultBuilder();
            resultBuilder.ifError(() -> !original.getCloudPlatform().equals(newCred.getCloudPlatform()),
                    "CloudPlatform of the credential cannot be changed.");
            if (isAwsCredential(original, newCred)) {
                validateKeyBasedRoleBasedChange(original, newCred, resultBuilder);
            }
            return resultBuilder.build();
        } catch (IOException e) {
            throw new BadRequestException(e);
        }
    }

    private boolean isAwsCredential(Credential original, Credential newCred) {
        return CloudPlatform.AWS.name().equals(original.getCloudPlatform()) && CloudPlatform.AWS.name().equals(newCred.getCloudPlatform());
    }

    private void validateKeyBasedRoleBasedChange(Credential original, Credential newCred, ValidationResult.ValidationResultBuilder resultBuilder) throws IOException {
        JsonNode originalKeyBased = JsonUtil.readTree(original.getAttributes()).get("aws").get("keyBased");
        JsonNode originalRoleBased = JsonUtil.readTree(original.getAttributes()).get("aws").get("roleBased");
        JsonNode newKeyBased = JsonUtil.readTree(newCred.getAttributes()).get("aws").get("keyBased");
        JsonNode newRoleBased = JsonUtil.readTree(newCred.getAttributes()).get("aws").get("roleBased");
        resultBuilder.ifError(() -> !originalKeyBased.isNull() && !newRoleBased.isNull(),
                "Cannot change AWS credential from key based to role based.");
        resultBuilder.ifError(() -> !originalRoleBased.isNull() && !newKeyBased.isNull(),
                "Cannot change AWS credential from role based to key based.");
    }

}
