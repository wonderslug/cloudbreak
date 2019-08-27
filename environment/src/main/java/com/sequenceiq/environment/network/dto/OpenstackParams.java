package com.sequenceiq.environment.network.dto;

public class OpenstackParams {

    private String vpcId;

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public static final class OpenstackParamsBuilder {
        private String vpcId;

        private OpenstackParamsBuilder() {
        }

        public static OpenstackParamsBuilder anOpenstackParams() {
            return new OpenstackParamsBuilder();
        }

        public OpenstackParamsBuilder withVpcId(String vpcId) {
            this.vpcId = vpcId;
            return this;
        }

        public OpenstackParams build() {
            OpenstackParams openstackParams = new OpenstackParams();
            openstackParams.setVpcId(vpcId);
            return openstackParams;
        }
    }
}
