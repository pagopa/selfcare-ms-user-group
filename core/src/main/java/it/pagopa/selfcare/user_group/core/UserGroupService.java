package it.pagopa.selfcare.user_group.core;

import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;

import java.util.UUID;

public interface UserGroupService {

    UserGroupOperations createGroup(UserGroupOperations group);

    UserGroupOperations addMember(String id, UUID memberId);

    void deleteGroup(String id);

    void suspendGroup(String id);

    void activateGroup(String id);

    UserGroupOperations updateGroup(String id, UserGroupOperations group);
}
