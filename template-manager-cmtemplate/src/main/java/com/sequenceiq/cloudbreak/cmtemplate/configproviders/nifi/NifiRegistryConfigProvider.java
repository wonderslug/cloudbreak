package com.sequenceiq.cloudbreak.cmtemplate.configproviders.nifi;

import static com.sequenceiq.cloudbreak.cmtemplate.CMRepositoryVersionUtil.CLOUDERAMANAGER_VERSION_7_0_1;
import static com.sequenceiq.cloudbreak.cmtemplate.CMRepositoryVersionUtil.isVersionNewerOrEqualThanLimited;
import static com.sequenceiq.cloudbreak.cmtemplate.configproviders.ConfigUtils.config;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.cloudera.api.swagger.model.ApiClusterTemplateConfig;
import com.sequenceiq.cloudbreak.auth.altus.UmsRight;
import com.sequenceiq.cloudbreak.auth.altus.VirtualGroupRequest;
import com.sequenceiq.cloudbreak.auth.altus.VirtualGroupService;
import com.sequenceiq.cloudbreak.cmtemplate.configproviders.AbstractRoleConfigProvider;
import com.sequenceiq.cloudbreak.template.TemplatePreparationObject;

@Component
public class NifiRegistryConfigProvider extends AbstractRoleConfigProvider {
    @Inject
    private VirtualGroupService virtualGroupService;

    @Override
    public List<ApiClusterTemplateConfig> getRoleConfigs(String roleType, TemplatePreparationObject source) {
        List<ApiClusterTemplateConfig> configList = new ArrayList<>();
        String cdhVersion = source.getBlueprintView().getProcessor().getStackVersion() == null ?
                "" : source.getBlueprintView().getProcessor().getStackVersion();
        if (isVersionNewerOrEqualThanLimited(cdhVersion, CLOUDERAMANAGER_VERSION_7_0_1)) {
            VirtualGroupRequest virtualGroupRequest = source.getVirtualGroupRequest();
            String adminGroup = virtualGroupService.getVirtualGroup(virtualGroupRequest, UmsRight.NIFI_REGISTRY_ADMIN.getRight());
            configList.add(config("nifi.registry.initial.admin.groups", adminGroup));
        }
        return configList;
    }

    @Override
    public String getServiceType() {
        return "NIFIREGISTRY";
    }

    @Override
    public List<String> getRoleTypes() {
        return List.of("NIFI_REGISTRY_SERVER");
    }

}
