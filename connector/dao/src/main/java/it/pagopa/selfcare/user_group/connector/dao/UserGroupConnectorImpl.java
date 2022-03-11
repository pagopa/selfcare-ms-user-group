package it.pagopa.selfcare.user_group.connector.dao;


import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.pagopa.selfcare.user_group.connector.api.UserGroupConnector;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.dao.model.UserGroupEntity;
import it.pagopa.selfcare.user_group.connector.exception.ResourceAlreadyExistsException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceUpdateException;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        log.trace("insert start");
        log.debug("insert entity = {}", entity);
        UserGroupEntity insert;
        try {
            insert = repository.insert(new UserGroupEntity(entity));
        } catch (DuplicateKeyException e) {
            throw new ResourceAlreadyExistsException("UserGroup id = " + entity.getId(), e);
        }

        log.trace("insert end");
        return insert;
    }

    @Override
    public UserGroupOperations save(UserGroupOperations entity) {
        log.trace("save start");
        log.debug("save entity = {}", entity);
        UserGroupEntity result = repository.save(new UserGroupEntity(entity));
        log.debug("save result = {}", result);
        log.trace("save end");
        return result;
    }

    @Override
    public void insertMember(String id, String memberId) {
        log.trace("insertMember start");
        log.debug("insertMember id = {}, memberId = {}", id, memberId);

        UpdateResult updateResult = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)
                        .and("status").is(UserGroupStatus.ACTIVE)),
                new Update().push("members", memberId),
                UserGroupEntity.class);
        if (updateResult.getModifiedCount() == 0 && updateResult.getMatchedCount() == 1) {
            throw new ResourceUpdateException("Couldn't update resource");
        }
        log.trace("insertMember end");

    }

    @Override
    public void deleteMember(String id, String memberId) {
        log.trace("deleteMember start");
        log.debug("deleteMember id = {}, memberId = {}", id, memberId);

        UpdateResult updateResult = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)
                        .and("status").is(UserGroupStatus.ACTIVE)),
                new Update().pull("members", memberId),
                UserGroupEntity.class);
        if (updateResult.getModifiedCount() == 0 && updateResult.getMatchedCount() == 1) {
            throw new ResourceUpdateException("Couldn't update resource");
        }
        log.trace("insertMember end");
    }

    @Override
    public Optional<UserGroupOperations> findById(String id) {
        log.trace("findById start");
        log.debug("findById id = {} ", id);
        Optional<UserGroupOperations> result = repository.findById(id).map(Function.identity());
        log.debug("findById result = {}", result);
        log.trace("findById end");

        return result;
    }

    @Override
    public List<UserGroupOperations> findByInstitutionIdAndProductId(String institutionId, String productId, Pageable pageable) {
        log.trace("findByInstitutionIdAndProductId start");
        log.debug("findByInstitutionIdAndProductId institutionId= {} , productId = {}, pageable = {}", institutionId, productId, pageable);
        List<UserGroupOperations> result = repository.findByInstitutionIdAndProductId(institutionId, productId, pageable).stream().map(Function.identity()).collect(Collectors.toList());
        log.debug("findByInstitutionIdAndProductId result = {}", result);
        log.trace("findByInstitutionIdAndProductId end");
        return result;
    }

    @Override
    public void activateById(String id) {
        log.trace("activateById start");
        log.debug("activateById id = {} ", id);
        updateUserById(id, UserGroupStatus.ACTIVE);
        log.trace("activateById end");

    }


    @Override
    public void deleteById(String id) {
        log.trace("deleteById start");
        log.debug("deleteById id = {} ", id);
        DeleteResult result = mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), UserGroupEntity.class);
        if (result.getDeletedCount() == 0) {
            throw new ResourceNotFoundException();
        }
        log.trace("deleteById end");
    }

    @Override
    public void suspendById(String id) {
        log.trace("suspendById start");
        log.debug("suspendById id = {} ", id);
        updateUserById(id, UserGroupStatus.SUSPENDED);
        log.trace("suspendById end");

    }

    private void updateUserById(String id, UserGroupStatus status) {
        log.trace("updateUserById start");
        log.debug("updateUserById id = {}, status = {}", id, status);
        UpdateResult updateResult = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)),
                Update.update("status", status),
                UserGroupEntity.class);
        if (updateResult.getMatchedCount() == 0) {
            throw new ResourceNotFoundException();
        }
        log.trace("updateUserById end");

    }

}
