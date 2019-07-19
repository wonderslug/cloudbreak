package com.sequenceiq.cloudbreak.domain;

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sequenceiq.cloudbreak.common.json.Json;
import com.sequenceiq.cloudbreak.common.json.JsonToString;
import com.sequenceiq.cloudbreak.common.json.JsonUtil;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.workspace.model.Workspace;
import com.sequenceiq.cloudbreak.workspace.model.WorkspaceAwareResource;
import com.sequenceiq.cloudbreak.workspace.resource.WorkspaceResource;
import com.sequenceiq.common.model.FileSystemType;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"workspace_id", "name"}))
public class FileSystem implements ProvisionEntity, WorkspaceAwareResource {

    private static final TypeReference<StorageLocations> STORAGE_LOCATIONS_TYPE_REFERENCE = new TypeReference<>() {
    };

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "filesystem_generator")
    @SequenceGenerator(name = "filesystem_generator", sequenceName = "filesystem_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileSystemType type;

    @Column(nullable = false)
    private String description;

    @Convert(converter = JsonToString.class)
    @Column(columnDefinition = "TEXT")
    private Json configurations;

    @Convert(converter = JsonToString.class)
    @Column(columnDefinition = "TEXT")
    private Json locations;

    @ManyToOne
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cluster cluster;

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public WorkspaceResource getResource() {
        return WorkspaceResource.STACK;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileSystemType getType() {
        return type;
    }

    public void setType(FileSystemType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Json getLocations() {
        return locations;
    }

    public StorageLocations getLocationsObject() {
        try {
            if (locations != null && locations.getValue() != null) {
                return JsonUtil.readValue(locations.getValue(), STORAGE_LOCATIONS_TYPE_REFERENCE);
            }
            return null;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setLocations(StorageLocations storageLocations) {
        if (storageLocations != null) {
            this.locations = new Json(storageLocations);
        }
    }

    public Json getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Json configurations) {
        this.configurations = configurations;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }
}
