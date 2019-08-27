package com.sequenceiq.environment.network.dto;

public class GcpParams {

    private String vpcId;

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public static final class GcpParamsBuilder {
        private String vpcId;

        private GcpParamsBuilder() {
        }

        public static GcpParamsBuilder aGcpParams() {
            return new GcpParamsBuilder();
        }

        public GcpParamsBuilder withVpcId(String vpcId) {
            this.vpcId = vpcId;
            return this;
        }

        public GcpParams build() {
            GcpParams gcpParams = new GcpParams();
            gcpParams.setVpcId(vpcId);
            return gcpParams;
        }
    }
}
