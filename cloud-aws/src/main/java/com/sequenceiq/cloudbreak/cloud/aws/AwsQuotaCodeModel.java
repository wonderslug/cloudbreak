package com.sequenceiq.cloudbreak.cloud.aws;

public class AwsQuotaCodeModel {

    private String regex;

    private String code;

    public AwsQuotaCodeModel() {
    }

    public AwsQuotaCodeModel(String regex, String code) {
        this.regex = regex;
        this.code = code;
    }

    public String getRegex() {
        return regex;
    }

    public String getCode() {
        return code;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
