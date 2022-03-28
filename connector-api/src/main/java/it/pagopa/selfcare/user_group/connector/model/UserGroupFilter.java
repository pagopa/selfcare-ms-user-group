package it.pagopa.selfcare.user_group.connector.model;

import lombok.Data;

import java.util.Optional;

@Data
public class UserGroupFilter {
    private Optional<String> institutionId = Optional.empty();
    private Optional<String> productId = Optional.empty();
    private Optional<String> userId = Optional.empty();

    public void setInstitutionId(Optional<String> institutionId) {
        this.institutionId = institutionId == null ? Optional.empty() : institutionId;
    }

    public void setProductId(Optional<String> productId) {
        this.productId = productId == null ? Optional.empty() : productId;
    }

    public void setUserId(Optional<String> userId) {
        this.userId = userId == null ? Optional.empty() : userId;
    }
}
