package it.pagopa.selfcare.user_group.connector.model;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class UserGroupFilter {
    private Optional<String> institutionId;
    private Optional<String> productId;
    private Optional<String> userId;
    private Optional<UserGroupStatus> status;

    public static UserGroupFilterBuilder builder() {
        return new UserGroupFilterBuilder();
    }

    public static class UserGroupFilterBuilder {
        private UserGroupFilterBuilder() {
            this.productId = Optional.empty();
            this.institutionId = Optional.empty();
            this.userId = Optional.empty();
            this.status = Optional.empty();
        }
    }
}
