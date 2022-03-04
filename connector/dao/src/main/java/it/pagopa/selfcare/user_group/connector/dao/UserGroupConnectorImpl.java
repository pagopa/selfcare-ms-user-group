package it.pagopa.selfcare.user_group.connector.dao;


import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.UpdateResult;
import it.pagopa.selfcare.user_group.api.UserGroupConnector;
import it.pagopa.selfcare.user_group.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.dao.model.UserGroupEntity;
import it.pagopa.selfcare.user_group.exception.ResourceAlreadyExistsException;
import it.pagopa.selfcare.user_group.model.UserGroupStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserGroupConnectorImpl implements UserGroupConnector {

    private final UserGroupRepository repository;
    private final MongoTemplate mongoTemplate;


    @Autowired
    public UserGroupConnectorImpl(UserGroupRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public UserGroupOperations insert(UserGroupOperations entity) {
        UserGroupEntity insert;
        try {
            insert = repository.insert(new UserGroupEntity(entity));
        } catch (DuplicateKeyException e) {
            throw new ResourceAlreadyExistsException("UserGroup id = " + entity.getId(), e);
        }
        return insert;
    }

    @Override
    public Optional<UserGroupOperations> findById(String id) {
        //TODO
        return Optional.empty();
    }

    @Override
    public boolean deleteById(String id) {
        UpdateResult updateResult = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)
                        .and("status").ne(UserGroupStatus.DELETED)),
                Update.update("status", UserGroupStatus.DELETED)
                        .currentTimestamp("modifiedAt")
                        .addToSet("modifiedBy", Instant.now()),
                UserGroupEntity.class);
        return updateResult.getModifiedCount() == 1;
    }


    public boolean suspendById(String id) {
        UpdateResult updateResult = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)
                        .and("status").ne(UserGroupStatus.DELETED)),
                Update.update("status", UserGroupStatus.SUSPENDED),
                UserGroupEntity.class);
        return updateResult.getModifiedCount() == 1;
    }

}
