package com.sequenceiq.redbeams.repository;

import java.util.Set;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.data.repository.CrudRepository;

import com.sequenceiq.cloudbreak.workspace.repository.EntityType;
import com.sequenceiq.redbeams.domain.DatabaseServerConfig;

// FIXME: Use DisabledBaseRepository when doing permissions
@EntityType(entityClass = DatabaseServerConfig.class)
@Transactional(TxType.REQUIRED)
public interface DatabaseServerConfigRepository extends CrudRepository<DatabaseServerConfig, Long> {

    Set<DatabaseServerConfig> findAllByWorkspaceId(Long workspaceId);

}