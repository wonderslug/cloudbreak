package com.sequenceiq.cloudbreak.service.credential;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.dto.credential.Credential;
import com.sequenceiq.environment.api.v1.credential.endpoint.CredentialEndpoint;
import com.sequenceiq.environment.api.v1.credential.model.response.CredentialResponse;
import com.sequenceiq.environment.api.v1.environment.endpoint.EnvironmentEndpoint;
import com.sequenceiq.environment.api.v1.environment.model.response.DetailedEnvironmentResponse;

@Service
public class CredentialService {

//    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);

    @Inject
    private CredentialEndpoint credentialEndpoint;

    @Inject
    private EnvironmentEndpoint environmentEndpoint;

    @Inject
    private CredentialConverter credentialConverter;

    public Credential get(String crnOrName) {
        //TODO CloudPlatfrom needs to be part of the response
        //TODO Revise paramaters because most of them should be a secret
        CredentialResponse credentialResponse = credentialEndpoint.get(crnOrName);
        return credentialConverter.convert(credentialResponse);
    }

    public Credential getByEnvironmentNameOrCrn(String crnOrName) {
        DetailedEnvironmentResponse response = environmentEndpoint.get(crnOrName);
        //TODO DetailtEnvironmentResponse should contain the credential
        return get(response.getCredentialName());
    }
}