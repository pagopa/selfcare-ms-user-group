package it.pagopa.selfcare.user_group.core;

import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGroupService {

    UserGroupOperations createGroup(UserGroupOperations group);

    void addMember(String id, UUID memberId);

    void deleteMember(String groupId, String memberId);

    void deleteMembers(String userId, String institutionId, String memberId);

    UserGroupOperations getUserGroup(String id);

    List<UserGroupOperations> getUserGroups(Optional<String> institutionId, Optional<String> productId, Optional<String> userId, Optional<UserGroupStatus> status, Pageable pageable);

    void deleteGroup(String id);

    void suspendGroup(String id);

    void activateGroup(String id);

    UserGroupOperations updateGroup(String id, UserGroupOperations group);
}
