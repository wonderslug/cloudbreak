package com.sequenceiq.cloudbreak.cmtemplate.configproviders.nifi;

import static com.sequenceiq.cloudbreak.cmtemplate.configproviders.ConfigUtils.config;
import static com.sequenceiq.cloudbreak.template.VolumeUtils.buildVolumePathStringZeroVolumeHandled;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cloudera.api.swagger.model.ApiClusterTemplateConfig;
import com.sequenceiq.cloudbreak.cmtemplate.CmHostGroupRoleConfigProvider;
import com.sequenceiq.cloudbreak.template.TemplatePreparationObject;
import com.sequenceiq.cloudbreak.template.views.HostgroupView;

@Component
public class NifiVolumeConfigProvider implements CmHostGroupRoleConfigProvider {

    @Override
    public List<ApiClusterTemplateConfig> getRoleConfigs(String roleType, HostgroupView hostGroupView, TemplatePreparationObject source) {
        Integer volumeCount = Objects.nonNull(hostGroupView) ? hostGroupView.getVolumeCount() : 0;
        return List.of(
                config("nifi_flowfile_repository_directory", buildVolumePathStringZeroVolumeHandled(volumeCount, "flowfile-repo")),
                config("nifi_content_repository_directory_default", buildVolumePathStringZeroVolumeHandled(volumeCount, "content-repo")),
                config("nifi_provenance_repository_directory_default", buildVolumePathStringZeroVolumeHandled(volumeCount, "provenance-repo")),
                config("log_dir", buildVolumePathStringZeroVolumeHandled(volumeCount, "nifi-log")),
                config("nifi_database_directory", buildVolumePathStringZeroVolumeHandled(volumeCount, "database-dir"))
        );
    }

    @Override
    public String getServiceType() {
        return "NIFI";
    }

    @Override
    public Set<String> getRoleTypes() {
        return Set.of("NIFI_NODE");
    }
}
