package it.pagopa.selfcare.user_group.connector.dao;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.dao.auditing.SpringSecurityAuditorAware;
import it.pagopa.selfcare.user_group.connector.dao.model.UserGroupEntity;
import it.pagopa.selfcare.user_group.connector.exception.ResourceAlreadyExistsException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceUpdateException;
import it.pagopa.selfcare.user_group.connector.model.UserGroupFilter;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import org.bson.BsonValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;

import javax.validation.ValidationException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static it.pagopa.selfcare.commons.utils.TestUtils.mockInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserGroupConnectorImplTest {

    private final static String LOGGED_USER_ID = "id";

    private final UserGroupRepository repositoryMock;

    @AfterEach
    void clear() {
        repositoryMock.deleteAll();
        Mockito.reset(repositoryMock, mongoTemplateMock);
    }

    private final MongoTemplate mongoTemplateMock;

    private final UserGroupConnectorImpl groupConnector;

    private final SelfCareUser selfCareUser;

    public UserGroupConnectorImplTest() {
        selfCareUser = SelfCareUser.builder(LOGGED_USER_ID).build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        repositoryMock = Mockito.mock(UserGroupRepository.class);
        mongoTemplateMock = Mockito.mock(MongoTemplate.class);
        groupConnector = new UserGroupConnectorImpl(repositoryMock, mongoTemplateMock, new SpringSecurityAuditorAware());
    }

    @Test
    void insert_duplicateKey() {
        UserGroupEntity entity = mockInstance(new UserGroupEntity());
        Mockito.doThrow(DuplicateKeyException.class)
                .when(repositoryMock)
                .insert(Mockito.any(UserGroupEntity.class));
        //when
        Executable executable = () -> groupConnector.insert(entity);
        //then
        ResourceAlreadyExistsException e = assertThrows(ResourceAlreadyExistsException.class, executable);
        assertEquals("Failed _id or unique index constraint.", e.getMessage());
        verify(repositoryMock, Mockito.times(1))
                .insert(entity);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void insert() {
        //given
        UserGroupEntity entity = mockInstance(new UserGroupEntity());
        when(repositoryMock.insert(Mockito.any(UserGroupEntity.class)))
                .thenReturn(entity);
        //when
        UserGroupOperations saved = groupConnector.insert(entity);
        //then
        Assertions.assertEquals(entity, saved);
        verify(repositoryMock, Mockito.times(1))
                .insert(entity);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void findById() {
        // given
        String id = "id";
        Optional<UserGroupEntity> entity = Optional.of(mockInstance(new UserGroupEntity()));
        when(repositoryMock.findById(Mockito.any()))
                .thenReturn(entity);
        // when
        Optional<UserGroupOperations> found = groupConnector.findById(id);
        // then
        Assertions.assertEquals(entity, found);
        verify(repositoryMock, Mockito.times(1))
                .findById(id);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void findAll_fullyValued() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        String userId = "userId";
        UserGroupStatus allowedStatus = UserGroupStatus.ACTIVE;
        Pageable pageable = PageRequest.of(0, 3, Sort.by("name"));
        UserGroupFilter groupFilter = new UserGroupFilter();
        groupFilter.setUserId(Optional.of(userId));
        groupFilter.setInstitutionId(Optional.of(institutionId));
        groupFilter.setProductId(Optional.of(productId));
        groupFilter.setStatus(Optional.of(allowedStatus));
        List<UserGroupEntity> entities = List.of(mockInstance(new UserGroupEntity()));

        when(mongoTemplateMock.find(Mockito.any(Query.class), (Class<UserGroupEntity>) Mockito.any()))
                .thenReturn(entities);
        Instant now = Instant.now();
        //when
        List<UserGroupOperations> groups = groupConnector.findAll(groupFilter, pageable);
        //then
        assertEquals(1, groups.size());
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .find(queryCaptor.capture(), Mockito.any());
        Query query = queryCaptor.getValue();
        assertTrue(query.toString().contains(institutionId));
        assertTrue(query.toString().contains(userId));
        assertTrue(query.toString().contains(productId));
        assertTrue(query.toString().contains(allowedStatus.name()));
        verifyNoMoreInteractions(mongoTemplateMock);
    }

    @Test
    void findAll_fullyNull() {
        //given
        Pageable pageable = Pageable.unpaged();
        UserGroupFilter groupFilter = new UserGroupFilter();
        List<UserGroupEntity> entities = List.of(mockInstance(new UserGroupEntity()));
        when(mongoTemplateMock.find(Mockito.any(Query.class), (Class<UserGroupEntity>) Mockito.any()))
                .thenReturn(entities);
        //when
        List<UserGroupOperations> groups = groupConnector.findAll(groupFilter, pageable);
        //then
        assertEquals(1, groups.size());
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .find(queryCaptor.capture(), Mockito.any());
        Query query = queryCaptor.getValue();
        assertTrue(query.getFieldsObject().isEmpty());
        verifyNoMoreInteractions(mongoTemplateMock);
    }

    @Test
    void findAll_sortNotAllowedException() {
        //given
        Pageable pageable = PageRequest.of(0, 3, Sort.by("name"));
        UserGroupFilter groupFilter = new UserGroupFilter();
        //when
        Executable executable = () -> groupConnector.findAll(groupFilter, pageable);
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("Sorting not allowed without productId or institutionId", e.getMessage());
        verifyNoInteractions(mongoTemplateMock);
    }

    @Test
    void findAll_filterNotAllowedException() {
        //given
        Pageable pageable = Pageable.unpaged();
        UserGroupFilter filter = new UserGroupFilter();
        UserGroupStatus allowedStatus = UserGroupStatus.ACTIVE;
        filter.setStatus(Optional.of(allowedStatus));
        //when
        Executable executable = () -> groupConnector.findAll(filter, pageable);
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("At least one of productId, institutionId and userId must be provided with status filter", e.getMessage());
        verifyNoInteractions(mongoTemplateMock);
    }

    @Test
    void deleteById() {
        //given
        String groupId = "groupId";
        DeleteResult result = mockInstance(new DeleteResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getDeletedCount() {
                return 1;
            }
        });
        when(mongoTemplateMock.remove(Mockito.any(Query.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.deleteById(groupId);
        //then
        assertDoesNotThrow(executable);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        verify(mongoTemplateMock, Mockito.times(1))
                .remove(queryCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        assertEquals(groupId, query.getQueryObject().get(UserGroupEntity.Fields.id));
        verifyNoMoreInteractions(mongoTemplateMock);

    }

    @Test
    void deleteById_resourceNotFound() {
        //given
        String groupId = "groupId";
        DeleteResult result = mockInstance(new DeleteResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getDeletedCount() {
                return 0;
            }
        });
        when(mongoTemplateMock.remove(Mockito.any(Query.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.deleteById(groupId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .remove(queryCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        assertEquals(groupId, query.getQueryObject().get(UserGroupEntity.Fields.id));
        verifyNoMoreInteractions(mongoTemplateMock);
    }

    @Test
    void suspendById() {
        //given
        String groupId = "groupId";
        UpdateResult result = mockInstance(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 1;
            }

            @Override
            public long getModifiedCount() {
                return 1;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        Instant now = Instant.now();
        when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.suspendById(groupId);
        //then
        assertDoesNotThrow(executable);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .updateFirst(queryCaptor.capture(), updateCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        Update update = updateCaptor.getValue();
        Map<String, Object> set = (Map<String, Object>) update.getUpdateObject().get("$set");
        Map<String, Object> currentDate = (Map<String, Object>) update.getUpdateObject().get("$currentDate");
        assertEquals(groupId, query.getQueryObject().get(UserGroupEntity.Fields.id));
        assertEquals(UserGroupStatus.SUSPENDED, set.get("status"));
        assertEquals(selfCareUser.getId(), set.get("modifiedBy"));
        assertTrue(currentDate.containsKey("modifiedAt"));
        verifyNoMoreInteractions(mongoTemplateMock);


    }

    @Test
    void suspendById_resourceNotFound() {
        //given
        String groupId = "groupId";
        UpdateResult result = mockInstance(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 0;
            }

            @Override
            public long getModifiedCount() {
                return 1;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.suspendById(groupId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .updateFirst(queryCaptor.capture(), updateCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        Update update = updateCaptor.getValue();
        Map<String, Object> set = (Map<String, Object>) update.getUpdateObject().get("$set");
        Map<String, Object> currentDate = (Map<String, Object>) update.getUpdateObject().get("$currentDate");
        assertEquals(groupId, query.getQueryObject().get(UserGroupEntity.Fields.id));
        assertEquals(UserGroupStatus.SUSPENDED, set.get("status"));
        assertEquals(selfCareUser.getId(), set.get("modifiedBy"));
        assertTrue(currentDate.containsKey("modifiedAt"));
        verifyNoMoreInteractions(mongoTemplateMock);

    }

    @Test
    void activateById() {
        //given
        String groupId = "groupId";
        UpdateResult result = mockInstance(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 1;
            }

            @Override
            public long getModifiedCount() {
                return 1;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.activateById(groupId);
        //then
        assertDoesNotThrow(executable);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .updateFirst(queryCaptor.capture(), updateCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        Update update = updateCaptor.getValue();
        Map<String, Object> set = (Map<String, Object>) update.getUpdateObject().get("$set");
        Map<String, Object> currentDate = (Map<String, Object>) update.getUpdateObject().get("$currentDate");
        assertEquals(groupId, query.getQueryObject().get(UserGroupEntity.Fields.id));
        assertEquals(UserGroupStatus.ACTIVE, set.get("status"));
        assertEquals(selfCareUser.getId(), set.get("modifiedBy"));
        assertTrue(currentDate.containsKey("modifiedAt"));
        verifyNoMoreInteractions(mongoTemplateMock);


    }

    @Test
    void activateById_resourceNotFound() {
        //given
        String groupId = "groupId";
        UpdateResult result = mockInstance(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 0;
            }

            @Override
            public long getModifiedCount() {
                return 1;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.activateById(groupId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .updateFirst(queryCaptor.capture(), updateCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        Update update = updateCaptor.getValue();
        Map<String, Object> set = (Map<String, Object>) update.getUpdateObject().get("$set");
        Map<String, Object> currentDate = (Map<String, Object>) update.getUpdateObject().get("$currentDate");
        assertEquals(groupId, query.getQueryObject().get(UserGroupEntity.Fields.id));
        assertEquals(UserGroupStatus.ACTIVE, set.get("status"));
        assertEquals(selfCareUser.getId(), set.get("modifiedBy"));
        assertTrue(currentDate.containsKey("modifiedAt"));
        verifyNoMoreInteractions(mongoTemplateMock);
    }

    @Test
    void insertMember_updateError() {
        //given
        String groupId = "groupId";
        String memberId = UUID.randomUUID().toString();
        UpdateResult result = mockInstance(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 1;
            }

            @Override
            public long getModifiedCount() {
                return 0;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.insertMember(groupId, memberId);
        //then
        ResourceUpdateException resourceUpdateException = assertThrows(ResourceUpdateException.class, executable);
        assertEquals("Couldn't update resource", resourceUpdateException.getMessage());

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .updateFirst(queryCaptor.capture(), updateCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        Update update = updateCaptor.getValue();
        Map<String, Object> set = (Map<String, Object>) update.getUpdateObject().get("$set");
        Map<String, Object> currentDate = (Map<String, Object>) update.getUpdateObject().get("$currentDate");
        assertEquals(groupId, query.getQueryObject().get(UserGroupEntity.Fields.id));
        assertEquals(UserGroupStatus.ACTIVE, query.getQueryObject().get("status", UserGroupStatus.class));
        assertEquals(selfCareUser.getId(), set.get("modifiedBy"));
        assertTrue(currentDate.containsKey("modifiedAt"));
        verifyNoMoreInteractions(mongoTemplateMock);
    }

    @Test
    void insertMember() {
        //given
        String groupId = "groupId";
        String memberId = UUID.randomUUID().toString();

        UpdateResult result = mockInstance(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 1;
            }

            @Override
            public long getModifiedCount() {
                return 1;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.insertMember(groupId, memberId);
        //then
        assertDoesNotThrow(executable);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .updateFirst(queryCaptor.capture(), updateCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        Update update = updateCaptor.getValue();
        Map<String, Object> set = (Map<String, Object>) update.getUpdateObject().get("$set");
        Map<String, Object> currentDate = (Map<String, Object>) update.getUpdateObject().get("$currentDate");
        assertEquals(groupId, query.getQueryObject().get(UserGroupEntity.Fields.id));
        assertEquals(UserGroupStatus.ACTIVE, query.getQueryObject().get("status", UserGroupStatus.class));
        assertEquals(selfCareUser.getId(), set.get("modifiedBy"));
        assertTrue(currentDate.containsKey("modifiedAt"));
        verifyNoMoreInteractions(mongoTemplateMock);
    }

    @Test
    void deleteMember_updateError() {
        //given
        String groupId = "groupId";
        String memberId = UUID.randomUUID().toString();
        UpdateResult result = mockInstance(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 1;
            }

            @Override
            public long getModifiedCount() {
                return 0;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.deleteMember(groupId, memberId);
        //then
        ResourceUpdateException resourceUpdateException = assertThrows(ResourceUpdateException.class, executable);
        assertEquals("Couldn't update resource", resourceUpdateException.getMessage());
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .updateFirst(queryCaptor.capture(), updateCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        Update update = updateCaptor.getValue();
        Map<String, Object> set = (Map<String, Object>) update.getUpdateObject().get("$set");
        Map<String, Object> currentDate = (Map<String, Object>) update.getUpdateObject().get("$currentDate");
        assertEquals(groupId, query.getQueryObject().get(UserGroupEntity.Fields.id));
        assertEquals(UserGroupStatus.ACTIVE, query.getQueryObject().get("status", UserGroupStatus.class));
        assertEquals(selfCareUser.getId(), set.get("modifiedBy"));
        assertTrue(currentDate.containsKey("modifiedAt"));
        verifyNoMoreInteractions(mongoTemplateMock);
    }

    @Test
    void deleteMember() {
        //given
        String groupId = "groupId";
        String memberId = UUID.randomUUID().toString();

        UpdateResult result = mockInstance(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 1;
            }

            @Override
            public long getModifiedCount() {
                return 1;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        when(mongoTemplateMock.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.deleteMember(groupId, memberId);
        //then
        assertDoesNotThrow(executable);
        verify(mongoTemplateMock, Mockito.times(1))
                .updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any());
        verifyNoMoreInteractions(mongoTemplateMock);
    }

    @Test
    void deleteMembers_updateError() {
        //given
        String groupId = "groupId";
        String memberId = UUID.randomUUID().toString();
        String institutionId = "institutionId";
        String productId = "productId";
        UpdateResult result = mockInstance(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 1;
            }

            @Override
            public long getModifiedCount() {
                return 0;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        when(mongoTemplateMock.updateMulti(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.deleteMembers(memberId, institutionId, productId);
        //then
        ResourceUpdateException resourceUpdateException = assertThrows(ResourceUpdateException.class, executable);
        assertEquals("Couldn't update resource", resourceUpdateException.getMessage());
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .updateMulti(queryCaptor.capture(), updateCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        Update update = updateCaptor.getValue();
        Map<String, Object> set = (Map<String, Object>) update.getUpdateObject().get("$set");
        Map<String, Object> currentDate = (Map<String, Object>) update.getUpdateObject().get("$currentDate");
        assertEquals(memberId, query.getQueryObject().get(UserGroupEntity.Fields.members));
        assertEquals(institutionId, query.getQueryObject().get(UserGroupEntity.Fields.institutionId));
        assertEquals(productId, query.getQueryObject().get(UserGroupEntity.Fields.productId));
        assertEquals(selfCareUser.getId(), set.get("modifiedBy"));
        assertTrue(currentDate.containsKey("modifiedAt"));
        verifyNoMoreInteractions(mongoTemplateMock);
    }

    @Test
    void deleteMembers() {
        //given
        String memberId = UUID.randomUUID().toString();
        String institutionId = "institutionId";
        String productId = "productId";
        UpdateResult result = mockInstance(new UpdateResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getMatchedCount() {
                return 1;
            }

            @Override
            public long getModifiedCount() {
                return 1;
            }

            @Override
            public BsonValue getUpsertedId() {
                return null;
            }
        });
        when(mongoTemplateMock.updateMulti(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.deleteMembers(memberId, institutionId, productId);
        //then
        assertDoesNotThrow(executable);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, Mockito.times(1))
                .updateMulti(queryCaptor.capture(), updateCaptor.capture(), (Class<?>) Mockito.any());
        Query query = queryCaptor.getValue();
        Update update = updateCaptor.getValue();
        Map<String, Object> set = (Map<String, Object>) update.getUpdateObject().get("$set");
        Map<String, Object> currentDate = (Map<String, Object>) update.getUpdateObject().get("$currentDate");
        assertEquals(memberId, query.getQueryObject().get(UserGroupEntity.Fields.members));
        assertEquals(institutionId, query.getQueryObject().get(UserGroupEntity.Fields.institutionId));
        assertEquals(productId, query.getQueryObject().get(UserGroupEntity.Fields.productId));
        assertEquals(selfCareUser.getId(), set.get("modifiedBy"));
        assertTrue(currentDate.containsKey("modifiedAt"));
        verifyNoMoreInteractions(mongoTemplateMock);
    }

    @Test
    void save() {
        //given
        String id = "id";
        UserGroupEntity entity = mockInstance(new UserGroupEntity());
        when(repositoryMock.save(Mockito.any()))
                .thenReturn(entity);
        //when
        UserGroupOperations saved = groupConnector.save(entity);
        //then
        assertEquals(entity, saved);
        ArgumentCaptor<UserGroupEntity> entityCaptor = ArgumentCaptor.forClass(UserGroupEntity.class);
        verify(repositoryMock, Mockito.times(1))
                .save(entityCaptor.capture());
        UserGroupEntity capturedEntity = entityCaptor.getValue();
        assertEquals(entity, capturedEntity);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void save_duplicateKey() {
        //given
        String id = "id";
        UserGroupEntity entity = mockInstance(new UserGroupEntity());
        Mockito.doThrow(DuplicateKeyException.class)
                .when(repositoryMock)
                .save(Mockito.any(UserGroupEntity.class));
        //when
        Executable executable = () -> groupConnector.save(entity);
        //then
        ResourceAlreadyExistsException e = assertThrows(ResourceAlreadyExistsException.class, executable);
        assertEquals("Failed _id or unique index constraint.", e.getMessage());
        verify(repositoryMock, Mockito.times(1))
                .save(entity);
        verifyNoMoreInteractions(repositoryMock);
    }


}