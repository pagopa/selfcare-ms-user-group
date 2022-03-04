package it.pagopa.selfcare.user_group.core;

import it.pagopa.selfcare.user_group.api.UserGroupOperations;

public interface UserGroupService {

    UserGroupOperations createGroup(UserGroupOperations group);

    void deleteGroup(String id);
}
