package com.sequenceiq.freeipa.service.freeipa.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.GetRightsResponse;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.Group;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.MachineUser;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.User;
import com.sequenceiq.authorization.resource.ResourceAction;
import com.sequenceiq.cloudbreak.auth.altus.GrpcUmsClient;
import com.sequenceiq.cloudbreak.auth.altus.exception.UmsOperationException;
import com.sequenceiq.freeipa.service.freeipa.user.model.UmsState;
import com.sequenceiq.freeipa.service.freeipa.user.model.UmsState.Builder;

@Service
public class UmsUsersStateProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsUsersStateProvider.class);

    @Inject
    private GrpcUmsClient umsClient;

    public Map<String, UmsState> getUmsState(String accountId, String actorCrn, Set<String> environmentsFilter) {
        if (environmentsFilter == null || environmentsFilter.size() == 0) {
            LOGGER.error("Environment Filter argument is null of empty");
            throw new RuntimeException("Environment Filter argument is null of empty");
        }


        try {
            List<User> allUsers = umsClient.listAllUsers(actorCrn, accountId, Optional.empty());
            //allUsers.forEach(u -> umsStateBuilder.addUser(u, umsClient.getRightsForUser(actorCrn, u.getCrn(), null, Optional.empty())));

            List<MachineUser> allMachineUsers = umsClient.listAllMachineUsers(actorCrn, accountId, Optional.empty());
            //allMachineUsers.forEach(u -> umsStateBuilder.addMachineUser(u, umsClient.getRightsForUser(actorCrn, u.getCrn(), null, Optional.empty())));

            // TODO: No need to fetch groups
            List<Group> allGroups = umsClient.listAllGroups(actorCrn, accountId, Optional.empty());
            return getEnvToUmsStateMap(accountId,actorCrn,environmentsFilter,allUsers,allMachineUsers,allGroups);

        } catch (RuntimeException e) {
            throw new UmsOperationException(String.format("Error during UMS operation: %s", e.getMessage()));
        }
    }

    public Map<String, UmsState> getUserFilteredUmsState(String accountId, String actorCrn, Set<String> userCrns, Set<String> environmentsFilter) {
        // TODO allow filtering on machine users as well once that's exposed in the API
        UmsState.Builder umsStateBuilder = new UmsState.Builder();
        try {
            Set<String> groupCrns = new HashSet<>();
            List<User> users = umsClient.listUsers(actorCrn, accountId, List.copyOf(userCrns), Optional.empty());
            users.forEach(u -> {
                // TODO: No need of Rights call here
                GetRightsResponse rights = umsClient.getRightsForUser(actorCrn, u.getCrn(), null, Optional.empty());
                // below line is used in common private method
                //umsStateBuilder.addUser(u, rights);
                groupCrns.addAll(rights.getGroupCrnList());
            });

            List<Group> groups = umsClient.listGroups(actorCrn, accountId, List.copyOf(groupCrns), Optional.empty());

            return getEnvToUmsStateMap(accountId,actorCrn,environmentsFilter,users,null,groups);
        } catch (RuntimeException e) {
            throw new UmsOperationException(String.format("Error during UMS operation: %s", e.getMessage()));
        }
    }



    private Map<String, UmsState> getEnvToUmsStateMap(String accountId, String actorCrn,
                                                      Set<String> environmentsFilter, List<User> users,
                                                      List<MachineUser> machineUsers, List<Group> groups ) {


        UmsState.Builder umsStateBuilder = new UmsState.Builder();

        // TODO: No need of CDP Groups to be sync'ed
        groups.stream().forEach(g -> umsStateBuilder.addGroup(g));

        // process each user and update environmentCRN -> UmsState map
        Map<String, UmsState> envUmsStateMap = new HashMap<>();

        environmentsFilter.stream().forEach(envCRN -> {
            processForEnvironmentRights(umsStateBuilder, actorCrn, envCRN, users, machineUsers);
            envUmsStateMap.put(envCRN,umsStateBuilder.build());
        });

        return envUmsStateMap;
    }


    private void processForEnvironmentRights(Builder umsStateBuilder, String actorCrn, String envCRN, List<User> allUsers, List<MachineUser> allMachineUsers) {

        // for all users, check right for the passed envCRN

        for (User u : allUsers) {

            // TODO: Remove commented code
//            GetRightsResponse rights = umsClient.getRightsForUser(actorCrn, u.getCrn(), envCRN, Optional.empty());
//            // check if user has right for this env
//            List<ResourceRoleAssignment> assignedResourceRoles = rights.getResourceRolesAssignmentList();
//            if (rights.getThunderheadAdmin()) {
//                umsStateBuilder.addAdminUser(u);
//                continue;
//
//            }
//
//            if (assignedResourceRoles == null || assignedResourceRoles.size() == 0) {
//                continue;
//            }

            if (umsClient.checkRight(actorCrn, u.getCrn(), ResourceAction.WRITE.getAuthorizationName(), envCRN, Optional.empty())) {
                // this is admin user having write access
                umsStateBuilder.addAdminUser(u);
            } else if (umsClient.checkRight(actorCrn, u.getCrn(), ResourceAction.READ.getAuthorizationName(), envCRN, Optional.empty())) {
                // This is regular Environment user
                // TODO: Remove get Rights call, as its not needed anymore.
                umsStateBuilder.addUser(u, umsClient.getRightsForUser(actorCrn, u.getCrn(), envCRN, Optional.empty()));
            }

            // else no other user is for this environment.
        }
            // machine users

        for (MachineUser machineUser : allMachineUsers) {

            // TODO: Remove commented code
//            GetRightsResponse rights = umsClient.getRightsForUser(actorCrn, machineUser.getCrn(), envCRN, Optional.empty());
//            // check if user has right for this env
//            List<ResourceRoleAssignment> assignedResourceRoles = rights.getResourceRolesAssignmentList();
//            if (rights.getThunderheadAdmin()) {
//                umsStateBuilder.addAdminUser(u);
//                continue;
//
//            }
//
//            if (assignedResourceRoles == null || assignedResourceRoles.size() == 0) {
//                continue;
//            }

            // Machine User can be a power user also
            if (umsClient.checkRight(actorCrn, machineUser.getCrn(), ResourceAction.WRITE.getAuthorizationName(), envCRN, Optional.empty())) {
                // this is admin user having write access
                umsStateBuilder.addAdminMachineUser(machineUser);
            } else if (umsClient.checkRight(actorCrn, machineUser.getCrn(), ResourceAction.READ.getAuthorizationName(), envCRN, Optional.empty())) {
                // This is regular Environment user
                // TODO: Remove get Rights call, as its not needed anymore.
                umsStateBuilder.addMachineUser(machineUser, umsClient.getRightsForUser(actorCrn, machineUser.getCrn(), envCRN, Optional.empty()));
            }
            // else no other user is for this environment.
        }

    }
}
