package com.sequenceiq.cloudbreak.cloud.aws;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cb.aws.instancetypequotacodes")
public class AwsInstanceTypeQuotaCodesConfig {

    private List<AwsQuotaCodeModel> codes;

    public List<AwsQuotaCodeModel> getCodes() {
        return codes;
    }

    public void setCodes(List<AwsQuotaCodeModel> codes) {
        this.codes = codes;
    }
}
