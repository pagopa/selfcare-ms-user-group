package it.pagopa.selfcare.user_group.core;

import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;

public interface UserGroupService {

    UserGroupOperations createGroup(UserGroupOperations group);

    void deleteGroup(String id);

    void suspendGroup(String id);

    void activateGroup(String id);
}
