package it.pagopa.selfcare.user_group.connector.dao;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.user_group.connector.dao.config.DaoTestConfig;
import it.pagopa.selfcare.user_group.connector.dao.model.UserGroupEntity;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@EnableAutoConfiguration
@ContextConfiguration(classes = {UserGroupEntity.class, UserGroupRepository.class, DaoTestConfig.class})
class UserGroupRepositoryTest {

    @Autowired
    private UserGroupRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    void clear() {
        repository.deleteAll();
    }


    @Test
    void create() {
        //given
        Instant now = Instant.now().minusSeconds(1);
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UserGroupEntity group = TestUtils.mockInstance(new UserGroupEntity(), "setId",
                "setCreatedAt",
                "setCreateBy",
                "setModifiedAt",
                "setModifiedBy");
        //when
        UserGroupEntity savedGroup = repository.insert(group);
        //then
        assertTrue(now.isBefore(savedGroup.getCreatedAt()));
        assertEquals(selfCareUser.getId(), savedGroup.getCreatedBy());
        assertNull(savedGroup.getModifiedBy());
        assertNotNull(savedGroup, "id cannot be null after entity creation");
    }


    @Test
    void delete() {
        //given
        Instant now = Instant.now().minusSeconds(1);
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UserGroupEntity group = TestUtils.mockInstance(new UserGroupEntity(), "setId",
                "setCreatedAt",
                "setCreateBy",
                "setModifiedAt",
                "setModifiedBy");
        UserGroupEntity savedGroup = repository.insert(group);
        Optional<UserGroupEntity> found = repository.findById(savedGroup.getId());
        //when
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(savedGroup.getId())), UserGroupEntity.class);
        //then
        Optional<UserGroupEntity> deleted = repository.findById(savedGroup.getId());
        assertEquals(Optional.empty(), deleted);

    }

    @Test
    void update() {
        //given
        Instant now = Instant.now().minusSeconds(1);
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UserGroupEntity group = TestUtils.mockInstance(new UserGroupEntity(), "setId",
                "setCreatedAt",
                "setCreateBy",
                "setModifiedAt",
                "setModifiedBy");
        UserGroupEntity savedGroup = repository.insert(group);
        Optional<UserGroupEntity> groupMod = repository.findById(savedGroup.getId());
        groupMod.get().setId(savedGroup.getId());
        groupMod.get().setStatus(UserGroupStatus.SUSPENDED);
        //when
        UserGroupEntity modifiedGroup = repository.save(groupMod.get());
        //then
        assertTrue(modifiedGroup.getModifiedAt().isAfter(savedGroup.getCreatedAt()));
        assertEquals(UserGroupStatus.ACTIVE, savedGroup.getStatus());
        assertEquals(UserGroupStatus.SUSPENDED, modifiedGroup.getStatus());
    }


}