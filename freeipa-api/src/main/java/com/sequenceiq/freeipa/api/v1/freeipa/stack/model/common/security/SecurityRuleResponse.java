package com.sequenceiq.freeipa.api.v1.freeipa.stack.model.common.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("SecurityRuleV1Response")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityRuleResponse extends SecurityRuleBase {
}
