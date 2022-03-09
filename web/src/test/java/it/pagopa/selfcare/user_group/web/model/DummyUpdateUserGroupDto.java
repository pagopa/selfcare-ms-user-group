package it.pagopa.selfcare.user_group.web.model;

import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DummyUpdateUserGroupDto {
    private String name;
    private String description;
    private UserGroupStatus status;
    private List<UUID> members = List.of(UUID.randomUUID());
}
