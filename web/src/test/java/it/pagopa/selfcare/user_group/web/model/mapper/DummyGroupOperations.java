package it.pagopa.selfcare.user_group.web.model.mapper;

import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class DummyGroupOperations implements UserGroupOperations {
    private String id;
    private String institutionId;
    private String productId;
    private String name;
    private String description;
    private UserGroupStatus status;
    private List<UUID> members;
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;
}
