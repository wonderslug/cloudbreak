package com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base;


import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sequenceiq.cloudbreak.api.endpoint.v4.JsonEntity;

import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageLocationV4 implements JsonEntity {

    @ApiModelProperty
    @NotNull
    private String propertyFile;

    @ApiModelProperty
    @NotNull
    private String propertyName;

    @ApiModelProperty
    @NotNull
    private String value;

    public String getPropertyFile() {
        return propertyFile;
    }

    public void setPropertyFile(String propertyFile) {
        this.propertyFile = propertyFile;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StorageLocationV4)) {
            return false;
        }
        StorageLocationV4 that = (StorageLocationV4) o;
        return Objects.equals(propertyFile, that.propertyFile)
                && Objects.equals(propertyName, that.propertyName)
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyFile, propertyName, value);
    }

}
