package it.pagopa.selfcare.user_group.connector.dao;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.dao.model.UserGroupEntity;
import it.pagopa.selfcare.user_group.connector.exception.ResourceAlreadyExistsException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceUpdateException;
import org.bson.BsonValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserGroupConnectorImplTest {

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }

    @Mock
    private UserGroupRepository repositoryMock;

    @AfterEach
    void clear() {
        repositoryMock.deleteAll();
    }

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private UserGroupConnectorImpl groupConnector;


    @Test
    void insert_duplicateKey() {
        UserGroupEntity entity = TestUtils.mockInstance(new UserGroupEntity());
        Mockito.doThrow(DuplicateKeyException.class)
                .when(repositoryMock)
                .insert(Mockito.any(UserGroupEntity.class));
        //when
        Executable executable = () -> groupConnector.insert(entity);
        //then
        ResourceAlreadyExistsException e = assertThrows(ResourceAlreadyExistsException.class, executable);
        assertEquals("UserGroup id = " + entity.getId(), e.getMessage());
        Mockito.verify(repositoryMock, Mockito.times(1))
                .insert(entity);
        Mockito.verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void insert() {
        //given
        UserGroupEntity entity = TestUtils.mockInstance(new UserGroupEntity());
        Mockito.when(repositoryMock.insert(Mockito.any(UserGroupEntity.class)))
                .thenReturn(entity);
        //when
        UserGroupOperations saved = groupConnector.insert(entity);
        //then
        Assertions.assertEquals(entity, saved);
        Mockito.verify(repositoryMock, Mockito.times(1))
                .insert(entity);
        Mockito.verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void findById() {
        // given
        String id = "id";
        Optional<UserGroupEntity> entity = Optional.of(TestUtils.mockInstance(new UserGroupEntity()));
        Mockito.when(repositoryMock.findById(Mockito.any()))
                .thenReturn(entity);
        // when
        Optional<UserGroupOperations> found = groupConnector.findById(id);
        // then
        Assertions.assertEquals(entity, found);
        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(id);
        Mockito.verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void findByInstitutionIdAndProductId() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        Pageable pageable = PageRequest.of(0, 3, Sort.by("name"));
        List<UserGroupEntity> entities = List.of(TestUtils.mockInstance(new UserGroupEntity()), TestUtils.mockInstance(new UserGroupEntity()), TestUtils.mockInstance(new UserGroupEntity()));
        Mockito.when(repositoryMock.findByInstitutionIdAndProductId(Mockito.anyString(), Mockito.anyString(), Mockito.any())).
                thenReturn(entities);
        //when
        List<UserGroupOperations> groups = groupConnector.findByInstitutionIdAndProductId(institutionId, productId, pageable);
        //then
        assertEquals(3, groups.size());
        Mockito.verify(repositoryMock, Mockito.times(1))
                .findByInstitutionIdAndProductId(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        Mockito.verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    void deleteById() {
        //given
        String groupId = "groupId";
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        DeleteResult result = TestUtils.mockInstance(new DeleteResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getDeletedCount() {
                return 1;
            }
        });
        Mockito.when(mongoTemplate.remove(Mockito.any(Query.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.deleteById(groupId);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .remove(Mockito.any(Query.class), (Class<?>) Mockito.any());
        Mockito.verifyNoMoreInteractions(mongoTemplate);

    }

    @Test
    void deleteById_resourceNotFound() {
        //given
        String groupId = "groupId";
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        DeleteResult result = TestUtils.mockInstance(new DeleteResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getDeletedCount() {
                return 0;
            }
        });
        Mockito.when(mongoTemplate.remove(Mockito.any(Query.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.deleteById(groupId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .remove(Mockito.any(Query.class), (Class<?>) Mockito.any());
        Mockito.verifyNoMoreInteractions(mongoTemplate);
    }

    @Test
    void suspendById() {
        //given
        String groupId = "groupId";
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UpdateResult result = TestUtils.mockInstance(new UpdateResult() {
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
        Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.suspendById(groupId);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any());
        Mockito.verifyNoMoreInteractions(mongoTemplate);


    }

    @Test
    void suspendById_resourceNotFound() {
        //given
        String groupId = "groupId";
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UpdateResult result = TestUtils.mockInstance(new UpdateResult() {
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
        Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.suspendById(groupId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any());
        Mockito.verifyNoMoreInteractions(mongoTemplate);

    }

    @Test
    void activateById() {
        //given
        String groupId = "groupId";
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UpdateResult result = TestUtils.mockInstance(new UpdateResult() {
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
        Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.activateById(groupId);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any());
        Mockito.verifyNoMoreInteractions(mongoTemplate);


    }

    @Test
    void activateById_resourceNotFound() {
        //given
        String groupId = "groupId";
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UpdateResult result = TestUtils.mockInstance(new UpdateResult() {
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
        Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.activateById(groupId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any());
        Mockito.verifyNoMoreInteractions(mongoTemplate);
    }

    @Test
    void insertMember_updateError() {
        //given
        String groupId = "groupId";
        String memberId = UUID.randomUUID().toString();
        UpdateResult result = TestUtils.mockInstance(new UpdateResult() {
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
        Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.insertMember(groupId, memberId);
        //then
        ResourceUpdateException resourceUpdateException = assertThrows(ResourceUpdateException.class, executable);
        assertEquals("Couldn't update resource", resourceUpdateException.getMessage());

        Mockito.verify(mongoTemplate, Mockito.times(1))
                .updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any());
        Mockito.verifyNoMoreInteractions(mongoTemplate);
    }

    @Test
    void insertMember() {
        //given
        String groupId = "groupId";
        String memberId = UUID.randomUUID().toString();

        UpdateResult result = TestUtils.mockInstance(new UpdateResult() {
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
        Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.insertMember(groupId, memberId);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any());
        Mockito.verifyNoMoreInteractions(mongoTemplate);
    }

    @Test
    void deleteMember_updateError() {
        //given
        String groupId = "groupId";
        String memberId = UUID.randomUUID().toString();
        UpdateResult result = TestUtils.mockInstance(new UpdateResult() {
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
        Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.deleteMember(groupId, memberId);
        //then
        ResourceUpdateException resourceUpdateException = assertThrows(ResourceUpdateException.class, executable);
        assertEquals("Couldn't update resource", resourceUpdateException.getMessage());

        Mockito.verify(mongoTemplate, Mockito.times(1))
                .updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any());
        Mockito.verifyNoMoreInteractions(mongoTemplate);
    }

    @Test
    void deleteMember() {
        //given
        String groupId = "groupId";
        String memberId = UUID.randomUUID().toString();

        UpdateResult result = TestUtils.mockInstance(new UpdateResult() {
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
        Mockito.when(mongoTemplate.updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any()))
                .thenReturn(result);
        //when
        Executable executable = () -> groupConnector.deleteMember(groupId, memberId);
        //then
        assertDoesNotThrow(executable);
        Mockito.verify(mongoTemplate, Mockito.times(1))
                .updateFirst(Mockito.any(Query.class), Mockito.any(Update.class), (Class<?>) Mockito.any());
        Mockito.verifyNoMoreInteractions(mongoTemplate);
    }

    @Test
    void save() {
        //given
        String id = "id";
        UserGroupEntity entity = TestUtils.mockInstance(new UserGroupEntity());
        Mockito.when(repositoryMock.save(Mockito.any()))
                .thenReturn(entity);
        //when
        UserGroupOperations saved = groupConnector.save(entity);
        //then
        assertEquals(entity, saved);
        Mockito.verify(repositoryMock, Mockito.times(1))
                .save(entity);
        Mockito.verifyNoMoreInteractions(repositoryMock);
    }


}