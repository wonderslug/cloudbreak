package com.sequenceiq.cloudbreak.core.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.idbmms.GrpcIdbmmsClient;
import com.sequenceiq.cloudbreak.idbmms.model.Mappings;
import com.sequenceiq.cloudbreak.service.altus.AltusMachineUserService;
import com.sequenceiq.cloudbreak.service.blueprint.ComponentLocatorService;
import com.sequenceiq.cloudbreak.service.environment.EnvironmentClientService;
import com.sequenceiq.common.api.telemetry.model.Telemetry;
import com.sequenceiq.environment.api.v1.environment.model.response.DetailedEnvironmentResponse;

@Service
public class ClusterMachineUserService {

    @Inject
    private AltusMachineUserService altusMachineUserService;

    @Inject
    private EnvironmentClientService environmentClientService;

    @Inject
    private ComponentLocatorService componentLocatorService;

    @Inject
    private GrpcIdbmmsClient grpcIdbmmsClient;

    public void create(Stack stack) {
        if (nifiMachineUserCreationEnabled(stack)) {
            createNifiMahcineUser(stack);
        }
    }

    public void delete(Stack stack, Telemetry telemetry) {
        altusMachineUserService.clearFluentMachineUser(stack, telemetry);
        removeNifiMachineUser(stack);
    }

    private boolean nifiMachineUserCreationEnabled(Stack stack) {
        Map<String, List<String>> nifiComponentLocation = componentLocatorService.getComponentLocation(stack.getCluster(), List.of("NIFI_NODE"));
        boolean nifiComponentHasBeenFound = MapUtils.isNotEmpty(nifiComponentLocation);
        if (nifiComponentHasBeenFound) {
            DetailedEnvironmentResponse env = environmentClientService.getByCrn(stack.getEnvironmentCrn());
            return BooleanUtils.isTrue(env.getCreateNiFiMachineUser());
        }
        return false;
    }

    private void createNifiMahcineUser(Stack stack) {
        Optional<String> machineUserResponse = altusMachineUserService.generateNifiMachineUser(stack);
        if (machineUserResponse.isPresent()) {
            updateDataAccessRoleForMachineUser(stack, machineUserResponse.get(), MachineUserRoleUpdateOperation.ADD_ROLE);
        }
    }

    private void removeNifiMachineUser(Stack stack) {
        if (nifiMachineUserCreationEnabled(stack)) {
            String machineUserResponse = altusMachineUserService.getNifiMachineUserName(stack);
            updateDataAccessRoleForMachineUser(stack, machineUserResponse, MachineUserRoleUpdateOperation.REMOVE_ROLE);
            altusMachineUserService.clearNifiMachineUser(stack);
        }
    }

    private void updateDataAccessRoleForMachineUser(Stack stack, String machineUserCrn, MachineUserRoleUpdateOperation updateOperation) {
        String actorCrn = stack.getCreator().getUserCrn();
        String environmentCrn = stack.getEnvironmentCrn();
        Mappings actualMappings = getMappingsOfEnvironment(actorCrn, environmentCrn);
        updateMappingsOfMachineUserOnEnvironment(machineUserCrn, actorCrn, environmentCrn, actualMappings, updateOperation);
        //TODO polling of IDBMMS role syncs should be implemented
        grpcIdbmmsClient.syncMappings(actorCrn, environmentCrn);
    }

    private Mappings getMappingsOfEnvironment(String actorCrn, String environmentCrn) {
        Optional<String> requestId = Optional.of(UUID.randomUUID().toString());
        return grpcIdbmmsClient.getMappings(actorCrn, environmentCrn, requestId);
    }

    private void updateMappingsOfMachineUserOnEnvironment(String machineUserCrn, String actorCrn, String environmentCrn, Mappings actualMappings,
            MachineUserRoleUpdateOperation updateOperation) {
        long mappingsVersion = actualMappings.getMappingsVersion();
        Optional<String> requestId = Optional.of(UUID.randomUUID().toString());
        String dataAccessRole = actualMappings.getDataAccessRole();
        String baselineRole = actualMappings.getBaselineRole();
        Map<String, String> updatedMachineUserMappings = new HashMap<>(actualMappings.getMappings());

        switch (updateOperation) {
            case ADD_ROLE:
                updatedMachineUserMappings.put(machineUserCrn, dataAccessRole);
                break;
            case REMOVE_ROLE:
                updatedMachineUserMappings.remove(machineUserCrn);
                break;
        }

        Mappings updatedMappings = new Mappings(mappingsVersion, updatedMachineUserMappings, dataAccessRole, baselineRole);
        grpcIdbmmsClient.setMappings(actorCrn, environmentCrn, updatedMappings, requestId);
    }

    private enum MachineUserRoleUpdateOperation {
        ADD_ROLE,
        REMOVE_ROLE
    }
}
