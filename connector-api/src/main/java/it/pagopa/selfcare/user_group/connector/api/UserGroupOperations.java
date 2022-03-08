package it.pagopa.selfcare.user_group.connector.api;

import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserGroupOperations {

    String getId();

    void setId(String id);

    String getInstitutionId();

    void setInstitutionId(String institutionId);

    String getProductId();

    void setProductId(String productId);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    UserGroupStatus getStatus();

    void setStatus(UserGroupStatus status);

    List<UUID> getMembers();

    void setMembers(List<UUID> members);

    Instant getCreatedAt();

    void setCreatedAt(Instant createdAt);

    String getCreatedBy();

    void setCreatedBy(String createdBy);

    Instant getModifiedAt();

    void setModifiedAt(Instant modifiedAt);

    String getModifiedBy();

    void setModifiedBy(String modifiedBy);

}
