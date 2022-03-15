package it.pagopa.selfcare.user_group.connector.dao;

import it.pagopa.selfcare.user_group.connector.dao.model.UserGroupEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserGroupRepository extends MongoRepository<UserGroupEntity, String> {

    List<UserGroupEntity> findByInstitutionIdAndProductId(String institutionId, String productId, Pageable pageable);

}
