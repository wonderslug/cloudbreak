package com.sequenceiq.freeipa.service.freeipa.user;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.GetRightsResponse;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.Group;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.MachineUser;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.ResourceRoleAssignment;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.RoleAssignment;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.User;
import com.sequenceiq.cloudbreak.auth.altus.GrpcUmsClient;
import com.sequenceiq.cloudbreak.auth.altus.exception.UmsOperationException;
import com.sequenceiq.freeipa.service.freeipa.user.model.UmsState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UmsUsersStateProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsUsersStateProvider.class);

    @Inject
    private GrpcUmsClient umsClient;

    //    private final String environmentWrite = "environments/write";
    private final String environmentWrite = "environments/setPassword";

    public Map<String, UmsState> getEnvToUmsStateMap(
        String accountId, String actorCrn, Set<String> environmentsFilter, Set<String> userCrns, Set<String> machineUserCrns, Optional<String> requestId) {
        if (environmentsFilter == null || environmentsFilter.size() == 0) {
            LOGGER.error("Environment Filter argument is null of empty");
            throw new RuntimeException("Environment Filter argument is null of empty");
        }

        try {
            List<User> users = userCrns.isEmpty() ? umsClient.listAllUsers(actorCrn, accountId, Optional.empty())
                    : umsClient.listUsers(actorCrn, accountId, List.copyOf(userCrns), Optional.empty());
            List<MachineUser> machineUsers = machineUserCrns.isEmpty() ? umsClient.listAllMachineUsers(actorCrn, accountId, Optional.empty())
                    : umsClient.listMachineUsers(actorCrn, accountId, List.copyOf(machineUserCrns), Optional.empty());

            return getEnvToUmsStateMap(accountId, actorCrn, environmentsFilter, users, machineUsers, requestId);

        } catch (RuntimeException e) {
            throw new UmsOperationException(String.format("Error during UMS operation: %s", e.getMessage()));
        }
    }

    private Map<String, UmsState> getEnvToUmsStateMap(
        String accountId, String actorCrn, Set<String> environmentsFilter,
        List<User> allUsers, List<MachineUser> machineUsers, Optional<String> requestId) {
        UmsState.Builder umsStateBuilder = new UmsState.Builder();

        // process each user and update environmentCRN -> UmsState map
        Map<String, UmsState> envUmsStateMap = new HashMap<>();

        List<User> adminUsers = new ArrayList<>();
        List<User> filteredUsers = new ArrayList<>();
        List<MachineUser> machineAdminUsers = new ArrayList<>();
        List<MachineUser> filteredMachineUsers = new ArrayList<>();
        environmentsFilter.stream().forEach(envCRN -> {
            getUsersWithEnvironmentRights(actorCrn, envCRN, allUsers, requestId, adminUsers, filteredUsers);
            umsStateBuilder.addUsers(filteredUsers);

            // TODO: add this after David's code
            //umsStateBuilder.addAdminUser(adminUsers);


            getMachineUsersWithEnvironmentRights(actorCrn, envCRN, machineUsers, requestId, machineAdminUsers, filteredMachineUsers);
            umsStateBuilder.addMachineUsers(filteredMachineUsers);
            // TODO: add this after David's code
            //umsStateBuilder.addMachineAdminUser(machineAdminUsers);


            Map<String, List<Group>> userToGroupsMap = new HashMap<>();

            // get all groups for identified users those having rights.
            umsClient.getUsersToGroupsMap(userToGroupsMap, actorCrn, accountId, filteredUsers, Optional.empty());
            umsClient.getMachineUsersToGroupsMap(userToGroupsMap, actorCrn, accountId, filteredMachineUsers, Optional.empty());

            umsStateBuilder.addUserToGroupMap(userToGroupsMap);


            envUmsStateMap.put(envCRN, umsStateBuilder.build());
        });

        return envUmsStateMap;
    }

    private void getUsersWithEnvironmentRights(
        String actorCrn, String envCRN, List<User> allUsers, Optional<String> requestId, List<User> adminUsers, List<User> filteredUsers) {
        // for all users, check right for the passed envCRN
        for (User u : allUsers) {

            // 1. Get Rights,
            // 2. check Roles as Power User, add those to admin map
            // 3. get Resource Assigned Roles,
            // 4. get if Env Admin Role is there then add admin
            // 5. Check if Env User role is there, then add as user.
            GetRightsResponse rightResponse = umsClient.getRightsForUser(actorCrn, u.getCrn(), envCRN, requestId);
            List<RoleAssignment> rolesAssignedList = rightResponse.getRoleAssignmentList();
            for (RoleAssignment roleAssigned : rolesAssignedList) {
                // TODO: should come from IAM Roles and check against Role Object
                if (roleAssigned.getRole().getCrn().contains("PowerUser") ||
                    roleAssigned.getRole().getCrn().contains("EnvironmentAdmin")) {
                    adminUsers.add(u);
                    // admins are also users
                    filteredUsers.add(u);
                }
            }

            List<ResourceRoleAssignment> resourceRoleAssignedList = rightResponse.getResourceRolesAssignmentList();
            for (ResourceRoleAssignment resourceRoleAssigned : resourceRoleAssignedList) {
                // TODO: should come from IAM Roles and check against Role Object
                if (resourceRoleAssigned.getResourceRole().getCrn().contains("EnvironmentAdmin")) {
                    adminUsers.add(u);
                    // admins are also users
                    filteredUsers.add(u);
                } else if (resourceRoleAssigned.getResourceRole().getCrn().contains("EnvironmentUser")) {
                    filteredUsers.add(u);
                }
            }

            // TODO: This should be used once resource role capability is available.
//            if (umsClient.checkRight(actorCrn, u.getCrn(), environmentWrite, envCRN, Optional.empty())) {
//                rightfulUsers.add(u);
//            }
        }
    }

    private void getMachineUsersWithEnvironmentRights(
        String actorCrn, String envCRN, List<MachineUser> allMachineUsers,
        Optional<String> requestId, List<MachineUser> machineAdminUsers, List<MachineUser> filteredMachineUsers) {

        // machine users
        for (MachineUser machineUser : allMachineUsers) {

            GetRightsResponse rightResponse = umsClient.getRightsForUser(actorCrn, machineUser.getCrn(), envCRN, requestId);
            List<RoleAssignment> rolesAssignedList = rightResponse.getRoleAssignmentList();
            for (RoleAssignment roleAssigned : rolesAssignedList) {
                // TODO: should come from IAM Roles and check against Role Object
                if (roleAssigned.getRole().getCrn().contains("PowerUser") ||
                    roleAssigned.getRole().getCrn().contains("EnvironmentAdmin")) {
                    machineAdminUsers.add(machineUser);
                    // admins are also users
                    filteredMachineUsers.add(machineUser);
                }
            }

            List<ResourceRoleAssignment> resourceRoleAssignedList = rightResponse.getResourceRolesAssignmentList();
            for (ResourceRoleAssignment resourceRoleAssigned : resourceRoleAssignedList) {
                // TODO: should come from IAM Roles and check against Role Object
                if (resourceRoleAssigned.getResourceRole().getCrn().contains("EnvironmentAdmin")) {
                    machineAdminUsers.add(machineUser);
                    // admins are also users
                    filteredMachineUsers.add(machineUser);
                } else if (resourceRoleAssigned.getResourceRole().getCrn().contains("EnvironmentUser")) {
                    filteredMachineUsers.add(machineUser);
                }
            }


            // TODO: This should be used once resource role capability is available.
//            if (umsClient.checkRight(actorCrn, machineUser.getCrn(), environmentWrite, envCRN, Optional.empty())) {
//                // this is admin user having write access
//                rightfulMachineUsers.add(machineUser);
//            }
        }
    }
}
