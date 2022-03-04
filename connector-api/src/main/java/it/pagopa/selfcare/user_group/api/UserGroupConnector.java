package it.pagopa.selfcare.user_group.api;

import java.util.Optional;

public interface UserGroupConnector {
    UserGroupOperations insert(UserGroupOperations entity);

    Optional<UserGroupOperations> findById(String id);

    boolean deleteById(String id);
}
