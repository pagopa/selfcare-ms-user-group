package it.pagopa.selfcare.user_group.connector.dao;


import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.user_group.connector.api.UserGroupConnector;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.dao.model.UserGroupEntity;
import it.pagopa.selfcare.user_group.connector.exception.ResourceAlreadyExistsException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.function.Function;

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
    public UserGroupOperations save(UserGroupOperations entity) {
        return repository.save(new UserGroupEntity(entity));
    }

    @Override
    public Optional<UserGroupOperations> findById(String id) {
        return repository.findById(id).map(Function.identity());
    }

    @Override
    public void activateById(String id) {
        updateUserById(id, UserGroupStatus.ACTIVE);
    }


    @Override
    public void deleteById(String id) {
        DeleteResult result = mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), UserGroupEntity.class);
        if (result.getDeletedCount() == 0) {
            throw new ResourceNotFoundException();
        }
        repository.deleteById(id);
    }

    @Override
    public void suspendById(String id) {
        updateUserById(id, UserGroupStatus.SUSPENDED);
    }

    private void updateUserById(String id, UserGroupStatus status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.state(authentication != null, "Authentication is required");
        Assert.state(authentication.getPrincipal() instanceof SelfCareUser, "Not SelfCareUser principal");
        SelfCareUser principal = ((SelfCareUser) authentication.getPrincipal());

        UpdateResult updateResult = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)),
                Update.update("status", status)
                        .currentTimestamp("modifiedAt")
                        .addToSet("modifiedBy", principal.getId()),
                UserGroupEntity.class);
        if (updateResult.getMatchedCount() == 0) {
            throw new ResourceNotFoundException();
        }
    }

}
