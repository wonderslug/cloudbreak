package com.sequenceiq.cloudbreak.cmtemplate.configproviders.tez;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sequenceiq.cloudbreak.cloud.model.ClouderaManagerProduct;
import com.sequenceiq.cloudbreak.cloud.model.ClouderaManagerRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.cloudera.api.swagger.model.ApiClusterTemplateConfig;
import com.sequenceiq.cloudbreak.cmtemplate.CmTemplateProcessor;
import com.sequenceiq.cloudbreak.domain.StorageLocation;
import com.sequenceiq.cloudbreak.template.TemplatePreparationObject;
import com.sequenceiq.cloudbreak.template.TemplatePreparationObject.Builder;
import com.sequenceiq.cloudbreak.template.filesystem.StorageLocationView;
import com.sequenceiq.cloudbreak.template.filesystem.s3.S3FileSystemConfigurationsView;
import com.sequenceiq.cloudbreak.template.views.HostgroupView;
import com.sequenceiq.cloudbreak.util.FileReaderUtils;
import com.sequenceiq.common.api.filesystem.S3FileSystem;
import com.sequenceiq.common.api.type.InstanceGroupType;

@RunWith(MockitoJUnitRunner.class)
public class TezRoleConfigProviderTest {

    private final TezRoleConfigProvider underTest = new TezRoleConfigProvider();

    @Test
    public void testGetTezClientRoleConfigs() {
        validateClientConfig("s3a://hive/warehouse/external",
                "s3a://hive/warehouse/external/sys.db",
                "s3a://hive/user/tez/7.0.2-1.cdh7.0.2.p2.1711788/tez.tar.gz");
        validateClientConfig("s3a://hive/warehouse/external/",
                "s3a://hive/warehouse/external/sys.db",
                "s3a://hive/user/tez/7.0.2-1.cdh7.0.2.p2.1711788/tez.tar.gz");
    }

    @Test
    public void testGetTezClientRoleConfigsWhenNoStorageConfigured() {
        TemplatePreparationObject preparationObject = getTemplatePreparationObject();
        String inputJson = getBlueprintText("input/clouderamanager-ds.bp");
        CmTemplateProcessor cmTemplateProcessor = new CmTemplateProcessor(inputJson);

        Map<String, List<ApiClusterTemplateConfig>> roleConfigs = underTest.getRoleConfigs(cmTemplateProcessor, preparationObject);
        List<ApiClusterTemplateConfig> tezConfigs = roleConfigs.get("tez-GATEWAY-BASE");
        assertEquals(0, tezConfigs.size());
    }

    protected void validateClientConfig(String hmsExternalDirLocation, String protoDirLocation, String tezLibUri) {
        TemplatePreparationObject preparationObject = getTemplatePreparationObject(hmsExternalDirLocation);
        String inputJson = getBlueprintText("input/clouderamanager-ds.bp");
        CmTemplateProcessor cmTemplateProcessor = new CmTemplateProcessor(inputJson);

        Map<String, List<ApiClusterTemplateConfig>> roleConfigs = underTest.getRoleConfigs(cmTemplateProcessor, preparationObject);
        List<ApiClusterTemplateConfig> tezConfigs = roleConfigs.get("tez-GATEWAY-BASE");

        assertEquals(1, tezConfigs.size());
        assertEquals("tez-conf/tez-site.xml_client_config_safety_valve", tezConfigs.get(0).getName());
        assertTrue(tezConfigs.get(0).getValue().contains("<property><name>tez.history.logging.proto-base-dir</name><value>"
                + protoDirLocation + "</value></property>"));
        assertTrue(tezConfigs.get(0).getValue().contains("<property><name>tez.lib.uris</name><value>"
                + tezLibUri + "</value></property>"));
    }

    private TemplatePreparationObject getTemplatePreparationObject(String... locations) {
        HostgroupView master = new HostgroupView("master", 1, InstanceGroupType.GATEWAY, 1);
        HostgroupView worker = new HostgroupView("worker", 2, InstanceGroupType.CORE, 2);

        List<StorageLocationView> storageLocations = new ArrayList<>();
        if (locations.length >= 1) {
            StorageLocation hmsExternalWarehouseDir = new StorageLocation();
            hmsExternalWarehouseDir.setProperty("hive.metastore.warehouse.external.dir");
            hmsExternalWarehouseDir.setValue(locations[0]);
            storageLocations.add(new StorageLocationView(hmsExternalWarehouseDir));
        }
        S3FileSystemConfigurationsView fileSystemConfigurationsView =
                new S3FileSystemConfigurationsView(new S3FileSystem(), storageLocations, false);

        ArrayList<ClouderaManagerProduct> products = new ArrayList<>();
        ClouderaManagerProduct cdh = new ClouderaManagerProduct();
        cdh.setName("cdh");
        cdh.setVersion("7.0.2-1.cdh7.0.2.p2.1711788");
        products.add(cdh);

        return Builder.builder().withFileSystemConfigurationView(fileSystemConfigurationsView)
                .withProductDetails(new ClouderaManagerRepo(), products)
                .withHostgroupViews(Set.of(master, worker)).build();
    }

    private String getBlueprintText(String path) {
        return FileReaderUtils.readFileFromClasspathQuietly(path);
    }
}
