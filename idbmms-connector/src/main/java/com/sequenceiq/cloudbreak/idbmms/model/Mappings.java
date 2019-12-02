package com.sequenceiq.cloudbreak.idbmms.model;

import java.util.Collections;
import java.util.Map;

public class Mappings {
    /**
     * The version of the mappings.
     */
    private final long mappingsVersion;

    /*
     * A map of actor and group CRNs to cloud provider roles. Does not include mappings for data access services.
     */
    private final Map<String, String> mappings;

    /*
     * The cloud provider role to which data access services will be mapped (e.g., an ARN in AWS, a Resource ID in Azure).
     */
    private final String dataAccessRole;

    /*
     * The cloud provider role associated with the baseline instance identity (e.g., an ARN in AWS, a Resource ID in Azure).
     * Non-data access services that write to cloud storage will be mapped to this role.
     */
    private final String baselineRole;

    public Mappings(long mappingsVersion, Map<String, String> mappings, String dataAccessRole, String baselineRole) {
        this.mappingsVersion = mappingsVersion;
        this.mappings = mappings == null ? Collections.emptyMap() : Collections.unmodifiableMap(mappings);
        this.dataAccessRole = dataAccessRole;
        this.baselineRole = baselineRole;
    }

    public long getMappingsVersion() {
        return mappingsVersion;
    }

    public Map<String, String> getMappings() {
        return mappings;
    }

    public String getDataAccessRole() {
        return dataAccessRole;
    }

    public String getBaselineRole() {
        return baselineRole;
    }
}
