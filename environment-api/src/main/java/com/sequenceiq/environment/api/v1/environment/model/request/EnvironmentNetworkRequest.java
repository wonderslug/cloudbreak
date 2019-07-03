package com.sequenceiq.environment.api.v1.environment.model.request;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sequenceiq.environment.api.doc.environment.EnvironmentModelDescription;
import com.sequenceiq.environment.api.v1.environment.model.EnvironmentNetworkAwsParams;
import com.sequenceiq.environment.api.v1.environment.model.EnvironmentNetworkAzureParams;
import com.sequenceiq.environment.api.v1.environment.model.EnvironmentNetworkYarnParams;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "EnvironmentNetworkV1Request")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class EnvironmentNetworkRequest {
    @ApiModelProperty(value = EnvironmentModelDescription.SUBNET_IDS, required = true)
    private Set<String> subnetIds;

    private String networkCidr;

    @ApiModelProperty(EnvironmentModelDescription.AWS_SPECIFIC_PARAMETERS)
    private EnvironmentNetworkAwsParams aws;

    @ApiModelProperty(EnvironmentModelDescription.AZURE_SPECIFIC_PARAMETERS)
    private EnvironmentNetworkAzureParams azure;

    @ApiModelProperty(EnvironmentModelDescription.YARN_SPECIFIC_PARAMETERS)
    private EnvironmentNetworkYarnParams yarn;

    public Set<String> getSubnetIds() {
        return subnetIds;
    }

    public void setSubnetIds(Set<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    public String getNetworkCidr() {
        return networkCidr;
    }

    public void setNetworkCidr(String networkCidr) {
        this.networkCidr = networkCidr;
    }

    public EnvironmentNetworkAwsParams getAws() {
        return aws;
    }

    public void setAws(EnvironmentNetworkAwsParams aws) {
        this.aws = aws;
    }

    public EnvironmentNetworkAzureParams getAzure() {
        return azure;
    }

    public void setAzure(EnvironmentNetworkAzureParams azure) {
        this.azure = azure;
    }

    public EnvironmentNetworkYarnParams getYarn() {
        return yarn;
    }

    public void setYarn(EnvironmentNetworkYarnParams yarn) {
        this.yarn = yarn;
    }
}
