package it.pagopa.selfcare.user_group.connector.api;

import java.util.Optional;

public interface UserGroupConnector {
    UserGroupOperations insert(UserGroupOperations entity);

    UserGroupOperations save(UserGroupOperations entity);

    void insertMember(String id, String memberId);

    Optional<UserGroupOperations> findById(String id);

    void activateById(String id);

    void deleteById(String id);

    void suspendById(String id);
}
