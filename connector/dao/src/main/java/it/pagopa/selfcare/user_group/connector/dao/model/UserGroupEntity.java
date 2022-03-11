package it.pagopa.selfcare.user_group.connector.dao.model;


import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Document("userGroups")
public class UserGroupEntity implements UserGroupOperations {

    public UserGroupEntity(UserGroupOperations userGroup) {
        this();
        id = userGroup.getId();
        institutionId = userGroup.getInstitutionId();
        productId = userGroup.getProductId();
        name = userGroup.getName();
        description = userGroup.getDescription();
        status = userGroup.getStatus();
        members = userGroup.getMembers();
        createdAt = userGroup.getCreatedAt();
        createdBy = userGroup.getCreatedBy();
        modifiedAt = userGroup.getModifiedAt();
        modifiedBy = userGroup.getModifiedBy();
    }

    @Id
    private String id;
    private String institutionId;
    private String productId;
    private String name;
    private String description;
    private UserGroupStatus status;

    private Set<String> members;
    @CreatedDate
    private Instant createdAt;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private Instant modifiedAt;
    @LastModifiedBy
    private String modifiedBy;

}
