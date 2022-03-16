package it.pagopa.selfcare.user_group.core;

import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGroupService {

    UserGroupOperations createGroup(UserGroupOperations group);

    void addMember(String id, UUID memberId);

    void deleteMember(String groupId, UUID memberId);

    UserGroupOperations getUserGroup(String id);

    List<UserGroupOperations> getUserGroups(Optional<String> institutionId, Optional<String> productId, Optional<UUID> userId, Pageable pageable);

    void deleteGroup(String id);

    void suspendGroup(String id);

    void activateGroup(String id);

    UserGroupOperations updateGroup(String id, UserGroupOperations group);
}
