package com.sequenceiq.redbeams.api.endpoint.v4.stacks;

import com.sequenceiq.cloudbreak.common.mappable.Mappable;
import com.sequenceiq.cloudbreak.common.mappable.ProviderParametersBase;
// import com.sequenceiq.redbeams.doc.ModelDescriptions.DatabaseServerModelDescription;

import io.swagger.annotations.ApiModelProperty;

public class DatabaseServerV4Base extends ProviderParametersBase {

    // FIXME define DatabaseServerModelDescription

    // @ApiModelProperty(DatabaseServerModelDescription.AWS_PARAMETERS)
    @ApiModelProperty("AWS parameters")
    private AwsDatabaseServerV4Parameters aws;

    // @ApiModelProperty(DatabaseServerModelDescription.GCP_PARAMETERS)
    // private GcpDatabaseServerV4Parameters gcp;

    // @ApiModelProperty(DatabaseServerModelDescription.AZURE_PARAMETERS)
    // private AzureDatabaseServerV4Parameters azure;

    // @ApiModelProperty(DatabaseServerModelDescription.OPEN_STACK_PARAMETERS)
    // private OpenStackDatabaseServerV4Parameters openstack;

    // @ApiModelProperty(hidden = true)
    // private MockDatabaseServerV4Parameters mock;

    // @ApiModelProperty(hidden = true)
    // private YarnDatabaseServerV4Parameters yarn;

    public AwsDatabaseServerV4Parameters createAws() {
        if (aws == null) {
            aws = new AwsDatabaseServerV4Parameters();
        }
        return aws;
    }

    public void setAws(AwsDatabaseServerV4Parameters aws) {
        this.aws = aws;
    }

    public Mappable createGcp() {
        return null;
    }

    public Mappable createAzure() {
        return null;
    }

    public Mappable createOpenstack() {
        return null;
    }

    public Mappable createYarn() {
        return null;
    }

    public Mappable createMock() {
        return null;
    }

    public AwsDatabaseServerV4Parameters getAws() {
        return aws;
    }

}
