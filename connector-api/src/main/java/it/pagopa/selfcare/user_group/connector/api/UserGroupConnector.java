package it.pagopa.selfcare.user_group.connector.api;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserGroupConnector {
    UserGroupOperations insert(UserGroupOperations entity);

    UserGroupOperations save(UserGroupOperations entity);

    void insertMember(String id, String memberId);

    void deleteMember(String id, String memberId);

    Optional<UserGroupOperations> findById(String id);

    List<UserGroupOperations> findByInstitutionIdAndProductId(String institutionId, String productId, Pageable pageable);

    void activateById(String id);

    void deleteById(String id);

    void suspendById(String id);
}
