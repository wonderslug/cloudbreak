package com.sequenceiq.cloudbreak.cmtemplate.configproviders.tez;

import com.cloudera.api.swagger.model.ApiClusterTemplateConfig;
import com.google.common.annotations.VisibleForTesting;
import com.sequenceiq.cloudbreak.cloud.model.ClouderaManagerProduct;
import com.sequenceiq.cloudbreak.cmtemplate.CmTemplateProcessor;
import com.sequenceiq.cloudbreak.cmtemplate.configproviders.AbstractRoleConfigProvider;
import com.sequenceiq.cloudbreak.cmtemplate.configproviders.ConfigUtils;
import com.sequenceiq.cloudbreak.template.TemplatePreparationObject;
import com.sequenceiq.cloudbreak.template.filesystem.StorageLocationView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.sequenceiq.cloudbreak.cmtemplate.configproviders.ConfigUtils.config;
import static com.sequenceiq.cloudbreak.cmtemplate.configproviders.ConfigUtils.getBasePathFromStorageLocation;
import static com.sequenceiq.cloudbreak.cmtemplate.configproviders.hive.HiveMetastoreCloudStorageServiceConfigProvider.HMS_METASTORE_EXTERNAL_DIR;

@Component
public class TezRoleConfigProvider extends AbstractRoleConfigProvider {

    private static final String TEZ_CONF_CLIENT_SAFETY_VALVE = "tez-conf/tez-site.xml_client_config_safety_valve";

    private static final String TEZ_LOGGING_PROTO_BASE_DIR_PARAM = "tez.history.logging.proto-base-dir";

    private static final String TEZ_LOGGING_PROTO_BASE_DIR_SUFFIX = "/sys.db";

    private static final String TEZ_LIB_URIS = "tez.lib.uris";

    private static final String USER_TEZ_DIR = "/user/tez/";

    private static final String TEZ_TAR_GZ = "tez.tar.gz";

    @Override
    protected List<ApiClusterTemplateConfig> getRoleConfigs(String roleType, TemplatePreparationObject source) {
        switch (roleType) {
            case TezRoles.GATEWAY:
                Optional<StorageLocationView> storageLocationForServiceProperty =
                        ConfigUtils.getStorageLocationForServiceProperty(source, HMS_METASTORE_EXTERNAL_DIR);

                String tezLoggingUrisSafetyValueProperty  = storageLocationForServiceProperty
                        .map(location -> location.getValue().replaceAll("/?$", "") + TEZ_LOGGING_PROTO_BASE_DIR_SUFFIX)
                        .map(logDir -> ConfigUtils.getSafetyValveProperty(TEZ_LOGGING_PROTO_BASE_DIR_PARAM, logDir))
                        .orElse("");

                String tezLibUrisSafetyValueProperty = storageLocationForServiceProperty
                        .map(location -> getBasePathFromStorageLocation(location.getValue()))
                        .map(basePath -> ConfigUtils.getSafetyValveProperty(TEZ_LIB_URIS, getTezLibUri(basePath, source)))
                        .orElse("");

                String value = tezLoggingUrisSafetyValueProperty + tezLibUrisSafetyValueProperty;

                return "".equals(value) ? List.of() : List.of(config(TEZ_CONF_CLIENT_SAFETY_VALVE, value));
            default:
                return List.of();
        }
    }

    @VisibleForTesting
    static String getTezLibUri(String basePath, TemplatePreparationObject source) {
        basePath = basePath.replaceAll("/?$", "");
        basePath += USER_TEZ_DIR;
        basePath += getCdhVersion(source);
        basePath += TEZ_TAR_GZ;
        return basePath;
    }

    @VisibleForTesting
    static String getCdhVersion(TemplatePreparationObject source) {
        if (source.getProductDetailsView() != null && source.getProductDetailsView().getProducts() != null) {
            Optional<ClouderaManagerProduct> cdh = source.getProductDetailsView().getProducts()
                    .stream()
                    .filter(e -> e.getName().equalsIgnoreCase("CDH"))
                    .findFirst();
            if (cdh.isEmpty()) {
                return "";
            }
            return cdh.get().getVersion() + "/";
        }
        return "";
    }

    @Override
    public String getServiceType() {
        return TezRoles.TEZ;
    }

    @Override
    public List<String> getRoleTypes() {
        return List.of(TezRoles.GATEWAY);
    }

    @Override
    public boolean isConfigurationNeeded(CmTemplateProcessor cmTemplateProcessor, TemplatePreparationObject source) {
        return source.getFileSystemConfigurationView().isPresent()
                && cmTemplateProcessor.isRoleTypePresentInService(getServiceType(), getRoleTypes());
    }
}
