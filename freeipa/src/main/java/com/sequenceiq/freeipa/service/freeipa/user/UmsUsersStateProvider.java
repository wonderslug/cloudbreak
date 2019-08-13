package com.sequenceiq.freeipa.service.freeipa.user;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.GetRightsResponse;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.Group;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.MachineUser;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.User;
import com.sequenceiq.cloudbreak.auth.altus.GrpcUmsClient;
import com.sequenceiq.cloudbreak.auth.altus.exception.UmsOperationException;
import com.sequenceiq.freeipa.service.freeipa.user.model.FmsGroup;
import com.sequenceiq.freeipa.service.freeipa.user.model.FmsUser;
import com.sequenceiq.freeipa.service.freeipa.user.model.UsersState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UmsUsersStateProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsUsersStateProvider.class);

    // TODO refactor ThreadBasedRequestIdProvider and replace this ThreadLocal
    private static ThreadLocal<String> requestId = new ThreadLocal<>();

    @Inject
    private GrpcUmsClient umsClient;

    private final String environmentWrite = "environments/write";

    public Map<String, UsersState> getEnvToUmsUsersStateMap(String accountId, String actorCrn, Set<String> environmentsFilter,
                Set<String> userCrns, Set<String> machineUserCrns) {
        try {
            // TODO propagate requestId from originating request
            Optional<String> requestIdOptional = Optional.of(UUID.randomUUID().toString());
            requestId.set(requestIdOptional.get());
            LOGGER.debug("Getting UMS state for environments {} with requestId {}", environmentsFilter, requestIdOptional);

            Map<String, UsersState> envUsersStateMap = new HashMap<>();


            List<User> users = userCrns.isEmpty() ? umsClient.listAllUsers(actorCrn, accountId, requestIdOptional)
                    : umsClient.listUsers(actorCrn, accountId, List.copyOf(userCrns), requestIdOptional);
            List<MachineUser> machineUsers = machineUserCrns.isEmpty() ? umsClient.listAllMachineUsers(actorCrn, accountId, requestIdOptional)
                    : umsClient.listMachineUsers(actorCrn, accountId, List.copyOf(machineUserCrns), requestIdOptional);

            Map<String, FmsGroup> crnToFmsGroup = umsClient.listGroups(actorCrn, accountId, List.of(), requestIdOptional).stream()
                    .collect(Collectors.toMap(Group::getCrn, this::umsGroupToGroup));

            environmentsFilter.stream().forEach(envCRN -> {
                UsersState.Builder userStateBuilder = new UsersState.Builder();

                users.stream().forEach(u -> {
                    FmsUser fmsUser = umsUserToUser(u);
                    handleUser(userStateBuilder, crnToFmsGroup, actorCrn, accountId, u.getCrn(), fmsUser, envCRN);
                });

                machineUsers.stream().forEach(mu -> {
                    FmsUser fmsUser = umsMachineUserToUser(mu);
                    handleUser(userStateBuilder, crnToFmsGroup, actorCrn, accountId, mu.getCrn(), fmsUser, envCRN);
                });

                envUsersStateMap.put(envCRN, userStateBuilder.build());
            });

            return envUsersStateMap;
        } catch (RuntimeException e) {
            throw new UmsOperationException(String.format("Error during UMS operation: %s", e.getMessage()));
        } finally {
            requestId.remove();
        }
    }

    private boolean isEnvironmentUser(String enviromentCrn, GetRightsResponse rightsResponse) {
        // TODO
        return false;
    }

    private boolean isEnvironmentAdmin(String enviromentCrn, GetRightsResponse rightsResponse) {
        // TODO
        return false;
    }

    private void handleUser(UsersState.Builder userStateBuilder, Map<String, FmsGroup> crnToFmsGroup,
            String actorCrn, String accountId, String memberCrn, FmsUser fmsUser, String environmentCrn) {
        Optional<String> requestIdOptional = Optional.ofNullable(requestId.get());

        GetRightsResponse rightsResponse = umsClient.getRightsForUser(actorCrn, memberCrn, environmentCrn, requestIdOptional);
        if (isEnvironmentUser(environmentCrn, rightsResponse)) {
            userStateBuilder.addUser(fmsUser);
            // TODO get group membership from GetRightsResponse instead of separate call
            getGroupCrnsForMember(actorCrn, accountId, memberCrn).stream().forEach(gcrn -> {
                userStateBuilder.addMemberToGroup(crnToFmsGroup.get(gcrn).getName(), fmsUser.getName());
            });
            if (isEnvironmentAdmin(environmentCrn, rightsResponse)) {
                userStateBuilder.addMemberToGroup("admins", fmsUser.getName());
            }
        }
    }

    private List<User> getUsersWithEnvironmentRights(
        String actorCrn, String envCRN, List<User> allUsers) {
        Optional<String> requestIdOptional = Optional.ofNullable(requestId.get());

        List<User> rightfulUsers = new ArrayList<>();
        // for all users, check right for the passed envCRN
        for (User u : allUsers) {

            if (umsClient.checkRight(actorCrn, u.getCrn(), environmentWrite, envCRN, requestIdOptional)) {
                // if (true) {
                rightfulUsers.add(u);
            }
        }
        return rightfulUsers;

    }

    private List<MachineUser> getMachineUsersWithEnvironmentRights(
        String actorCrn, String envCRN, List<MachineUser> allMachineUsers) {
        Optional<String> requestIdOptional = Optional.ofNullable(requestId.get());

        List<MachineUser> rightfulMachineUsers = new ArrayList<>();
        // machine users
        for (MachineUser machineUser : allMachineUsers) {

            // Machine User can be a power user also
            if (umsClient.checkRight(actorCrn, machineUser.getCrn(), environmentWrite, envCRN, requestIdOptional)) {
                // this is admin user having write access
                rightfulMachineUsers.add(machineUser);
            }
        }

        return rightfulMachineUsers;
    }

    private List<String> getGroupCrnsForMember(
            String accountId, String actorCrn, String memberCrn) {
        Optional<String> requestIdOptional = Optional.ofNullable(requestId.get());

        return umsClient.listGroupsForMember(actorCrn, accountId, memberCrn, requestIdOptional);
    }

    private FmsUser umsUserToUser(User umsUser) {
        FmsUser fmsUser = new FmsUser();
        fmsUser.setName(umsUser.getWorkloadUsername());
        fmsUser.setFirstName(getOrDefault(umsUser.getFirstName(), "None"));
        fmsUser.setLastName(getOrDefault(umsUser.getLastName(), "None"));
        return fmsUser;
    }

    private String getOrDefault(String value, String other) {
        return (value == null || value.isBlank()) ? other : value;
    }

    private FmsUser umsMachineUserToUser(MachineUser umsMachineUser) {
        FmsUser fmsUser = new FmsUser();
        fmsUser.setName(umsMachineUser.getWorkloadUsername());
        // TODO what should the appropriate first and last name be for machine users?
        fmsUser.setFirstName("Machine");
        fmsUser.setLastName("User");
        return fmsUser;
    }

    private FmsGroup umsGroupToGroup(Group umsGroup) {
        FmsGroup fmsGroup = new FmsGroup();
        fmsGroup.setName(umsGroup.getGroupName());
        return fmsGroup;
    }

}
