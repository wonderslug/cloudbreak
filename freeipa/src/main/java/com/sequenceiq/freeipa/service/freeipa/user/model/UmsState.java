package com.sequenceiq.freeipa.service.freeipa.user.model;

import static java.util.Objects.requireNonNull;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.GetRightsResponse;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.Group;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.MachineUser;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UmsState {
    private Map<User, List<Group>> userToGroupsMap;

    // Regular users
    private Map<String, User> userMap;

    // Admin Users for an environment.
    private Map<String, User> adminUserMap;

    private Map<String, MachineUser> adminMachineUserMap = new HashMap<>();

    private Map<String, MachineUser> machineUserMap;

    public UmsState(Map<User, List<Group>> userToGroupsMap, Map<String, User> adminUserMap,
                    Map<String, MachineUser> adminMachineUserMap, Map<String, User> userMap,
                    Map<String, MachineUser> machineUserMap) {

        this.userToGroupsMap = userToGroupsMap;
        this.adminUserMap = requireNonNull(adminUserMap);
        this.adminMachineUserMap = adminMachineUserMap;
        this.userMap = requireNonNull(userMap);
        this.machineUserMap = requireNonNull(machineUserMap);
    }

    public UsersState getUsersState(String environmentCrn) {

        //TODO: parse env and get name of env
        String adminGrpName = "cpd_env_admin_ap-test-env";
        UsersState.Builder builder = new UsersState.Builder();

        Map<String, com.sequenceiq.freeipa.api.v1.freeipa.user.model.Group> crnToGroup = new HashMap<>();

        userToGroupsMap.entrySet()
            .forEach(e -> {
                // TODO: list of all groups to be added and also membership
                User u = e.getKey();
                com.sequenceiq.freeipa.api.v1.freeipa.user.model.User user = umsUserToUser(u);
                List<Group> groups = e.getValue();
                for (Group g : groups) {
                    com.sequenceiq.freeipa.api.v1.freeipa.user.model.Group group = umsGroupToGroup(g);
                    crnToGroup.put(g.getCrn(), group);
                    builder.addMemberToGroup(group.getName(), user.getName());
                    builder.addGroup(group);
                }
            });

        // Regular Users
        userMap.entrySet()
                .forEach(e -> {
                    com.sequenceiq.freeipa.api.v1.freeipa.user.model.User user = umsUserToUser(e.getValue());
                    builder.addUser(user);
                });


        // env Admin Users
        adminUserMap.entrySet()
            .forEach(adminUser -> {
                // Admin users are also regular users but must be added to specific group. Admin Users are already added as regular users
                com.sequenceiq.freeipa.api.v1.freeipa.user.model.User user = umsUserToUser(adminUser.getValue());
                // TODO remove `admins` membership once the group mapping is figured out (CB-2003, DISTX-95)
                // TODO: change admins group to cpd_env_admin_<#env_name>, need to parse env crn and pass that group value.
                builder.addMemberToGroup(adminGrpName, user.getName());
            });

        // TODO filter machine users by environment rights
        machineUserMap.entrySet()
                .forEach(e -> {
                    com.sequenceiq.freeipa.api.v1.freeipa.user.model.User user = umsMachineUserToUser(e.getValue());
                    builder.addUser(user);
                });

        // Machine Admin Users
        adminMachineUserMap.entrySet()
                .forEach(e -> {
                    com.sequenceiq.freeipa.api.v1.freeipa.user.model.User user = umsMachineUserToUser(e.getValue());
                    // TODO remove `admins` membership once the group mapping is figured out (CB-2003, DISTX-95)
                    // TODO: change admins group to cpd_env_admin_<#env_name>, need to parse env crn and pass that group value.
                    builder.addMemberToGroup(adminGrpName, user.getName());
                });

        return builder.build();
    }

    public Set<String> getUsernamesFromCrns(Set<String> userCrns) {
        return userCrns.stream()
                .map(crn -> getWorkloadUsername(userMap.get(crn)))
                .collect(Collectors.toSet());
    }

    private com.sequenceiq.freeipa.api.v1.freeipa.user.model.User umsUserToUser(User umsUser) {
        com.sequenceiq.freeipa.api.v1.freeipa.user.model.User user = new com.sequenceiq.freeipa.api.v1.freeipa.user.model.User();
        user.setName(getWorkloadUsername(umsUser));
        user.setFirstName(getOrDefault(umsUser.getFirstName(), "None"));
        user.setLastName(getOrDefault(umsUser.getLastName(), "None"));
        return user;
    }

    private String getOrDefault(String value, String other) {
        return (value == null || value.isBlank()) ? other : value;
    }

    private String getWorkloadUsername(User umsUser) {
        return umsUser.getWorkloadUsername();
    }

    private com.sequenceiq.freeipa.api.v1.freeipa.user.model.User umsMachineUserToUser(MachineUser umsMachineUser) {
        com.sequenceiq.freeipa.api.v1.freeipa.user.model.User user = new com.sequenceiq.freeipa.api.v1.freeipa.user.model.User();
        user.setName(umsMachineUser.getWorkloadUsername());
        // TODO what should the appropriate first and last name be for machine users?
        user.setFirstName("Machine");
        user.setLastName("User");
        return user;
    }

    private com.sequenceiq.freeipa.api.v1.freeipa.user.model.Group umsGroupToGroup(Group umsGroup) {
        com.sequenceiq.freeipa.api.v1.freeipa.user.model.Group group = new com.sequenceiq.freeipa.api.v1.freeipa.user.model.Group();
        group.setName(umsGroup.getGroupName());
        return group;
    }

    public static class Builder {
        private Map<String, Group> groupMap = new HashMap<>();

        private Map<User, List<Group>> userToGroupsMap = new HashMap<>();

        private Map<String, User> userMap = new HashMap<>();

//        private Map<String, GetRightsResponse> userRightsMap = new HashMap<>();

        private Map<String, User> adminUserMap = new HashMap<>();

//        private Map<String, GetRightsResponse> adminUserRightsMap = new HashMap<>();

        private Map<String, MachineUser> adminMachineUserMap = new HashMap<>();

        private Map<String, MachineUser> machineUserMap = new HashMap<>();

//        private Map<String, GetRightsResponse> machineUserRightsMap = new HashMap<>();

        public void addUserToGroupMap(Map<User, List<Group>> userToGroupsMap) {
            this.userToGroupsMap = userToGroupsMap;
        }

        public void addGroup(Group group) {
            groupMap.put(group.getCrn(), group);
        }

        public void addUser(User user, GetRightsResponse rights) {
            String userCrn = user.getCrn();
            userMap.put(userCrn, user);
//            userRightsMap.put(userCrn, rights);
        }

        public void addAdminUser(User user) {
            String userCrn = user.getCrn();
            adminUserMap.put(userCrn, user);
            //adminUserRightsMap.put(userCrn, rights);
        }

        public void addAdminMachineUser(MachineUser machineAdminuser) {
            // Machine User can be a power user also.
            String userCrn = machineAdminuser.getCrn();
            adminMachineUserMap.put(userCrn, machineAdminuser);
        }

        public void addMachineUser(MachineUser machineUser, GetRightsResponse rights) {
            String machineUserCrn = machineUser.getCrn();
            machineUserMap.put(machineUserCrn, machineUser);
//            machineUserRightsMap.put(machineUserCrn, rights);
        }

        public UmsState build() {
            return new UmsState(userToGroupsMap, adminUserMap, adminMachineUserMap, userMap, machineUserMap);
        }
    }
}
