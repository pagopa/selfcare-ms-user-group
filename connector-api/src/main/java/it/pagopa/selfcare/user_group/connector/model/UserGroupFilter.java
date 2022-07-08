package it.pagopa.selfcare.user_group.connector.model;

import lombok.Data;

import java.util.Optional;

@Data
public class UserGroupFilter {
    private Optional<String> institutionId;
    private Optional<String> productId;
    private Optional<String> userId;
    private Optional<UserGroupStatus> status;

    public static UserGroupFilterBuilder builder() {
        return new UserGroupFilterBuilder();
    }

    public static class UserGroupFilterBuilder {
        private Optional<String> institutionId;
        private Optional<String> productId;
        private Optional<String> userId;
        private Optional<UserGroupStatus> status;

        private UserGroupFilterBuilder() {
            this.productId = Optional.empty();
            this.institutionId = Optional.empty();
            this.userId = Optional.empty();
            this.status = Optional.empty();
        }

        public UserGroupFilterBuilder institutionId(Optional<String> institutionId) {
            this.institutionId = institutionId == null ? Optional.empty() : institutionId;
            return this;
        }

        public UserGroupFilterBuilder productId(Optional<String> productId) {
            this.productId = productId == null ? Optional.empty() : productId;
            return this;
        }

        public UserGroupFilterBuilder userId(Optional<String> userId) {
            this.userId = userId == null ? Optional.empty() : userId;
            return this;
        }

        public UserGroupFilterBuilder status(Optional<UserGroupStatus> status) {
            this.status = status == null ? Optional.empty() : status;
            return this;
        }

        public UserGroupFilter build() {
            UserGroupFilter filter = new UserGroupFilter();
            filter.status = status;
            filter.institutionId = institutionId;
            filter.productId = productId;
            filter.userId = userId;
            return filter;
        }
    }
}
