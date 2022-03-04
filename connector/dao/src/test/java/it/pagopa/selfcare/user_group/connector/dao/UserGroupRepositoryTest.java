package it.pagopa.selfcare.user_group.connector.dao;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.user_group.connector.dao.config.DaoTestConfig;
import it.pagopa.selfcare.user_group.connector.dao.model.UserGroupEntity;
import it.pagopa.selfcare.user_group.model.UserGroupStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@EnableAutoConfiguration
@ContextConfiguration(classes = {UserGroupEntity.class, UserGroupRepository.class, DaoTestConfig.class})
class UserGroupRepositoryTest {

    @Autowired
    private UserGroupRepository repository;

    @AfterEach
    void clear() {
        repository.deleteAll();
    }

    @Test
    void create() {
        //given
        UserGroupEntity group = TestUtils.mockInstance(new UserGroupEntity(), "setId");
        //when
        UserGroupEntity savedGroup = repository.insert(group);
        //then
        assertNotNull(savedGroup, "id cannot be null after entity creation");
    }

    @Test
    void logicalDelete() {
        //given
        UserGroupEntity group = TestUtils.mockInstance(new UserGroupEntity(), "setId");
        UserGroupEntity savedGroup = repository.insert(group);
        Optional<UserGroupEntity> foundGroup = repository.findById(savedGroup.getId());
        //when
        foundGroup.get().setStatus(UserGroupStatus.DELETED);
        UserGroupEntity deleted = repository.save(foundGroup.get());
        //then
        assertNotNull(savedGroup, "id cannot be null after entity creation");
    }
}