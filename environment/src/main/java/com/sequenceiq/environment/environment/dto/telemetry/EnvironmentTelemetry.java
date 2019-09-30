package com.sequenceiq.environment.environment.dto.telemetry;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvironmentTelemetry implements Serializable {

    private EnvironmentLogging logging;

    private Features features;

    private Map<String, Object> fluentAttributes;

    private String databusEndpoint;

    public EnvironmentLogging getLogging() {
        return logging;
    }

    public void setLogging(EnvironmentLogging logging) {
        this.logging = logging;
    }

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public Map<String, Object> getFluentAttributes() {
        return fluentAttributes;
    }

    public void setFluentAttributes(Map<String, Object> fluentAttributes) {
        this.fluentAttributes = fluentAttributes;
    }

    public String getDatabusEndpoint() {
        return databusEndpoint;
    }

    public void setDatabusEndpoint(String databusEndpoint) {
        this.databusEndpoint = databusEndpoint;
    }
}