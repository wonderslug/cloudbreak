package com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.cluster.storage;

public enum CloudStorageCdpService {

    ZEPPELIN_SERVER("ZEPPELIN_SERVER"),
    RESOURCE_MANAGER("RESOURCEMANAGER"),
    HIVE_METASTORE("HIVEMETASTORE"),
    RANGER_ADMIN("RANGER_ADMIN");

    private final String cmServiceName;

    CloudStorageCdpService(String cmServiceName) {
        this.cmServiceName = cmServiceName;
    }

    public String cmServiceName() {
        return cmServiceName;
    }
}
