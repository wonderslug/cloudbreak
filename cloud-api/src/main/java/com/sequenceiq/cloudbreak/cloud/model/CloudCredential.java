package com.sequenceiq.cloudbreak.cloud.model;

import java.util.HashMap;
import java.util.Map;

import com.sequenceiq.cloudbreak.cloud.model.generic.DynamicModel;

public class CloudCredential extends DynamicModel {

    public static final String GOV_CLOUD = "govCloud";

    private final String credentialCrn;

    private final String name;

    public CloudCredential(String credentialCrn, String name) {
        this(credentialCrn, name, new HashMap<>());
    }

    public CloudCredential(String credentialCrn, String name, Map<String, Object> parameters) {
        super(parameters);
        this.credentialCrn = credentialCrn;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCredentialCrn() {
        return credentialCrn;
    }
}
