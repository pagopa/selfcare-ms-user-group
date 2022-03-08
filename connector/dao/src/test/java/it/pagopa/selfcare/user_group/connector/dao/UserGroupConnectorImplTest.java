package it.pagopa.selfcare.user_group.connector.dao;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.UpdateResult;
import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.dao.model.UserGroupEntity;
import it.pagopa.selfcare.user_group.connector.exception.ResourceAlreadyExistsException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void deleteById() {
        //given
        String groupId = "groupId";
        Mockito.doNothing()
                .when(repositoryMock)
                .deleteById(Mockito.any());
        //when
        groupConnector.deleteById(groupId);
        //then
        Mockito.verify(repositoryMock, Mockito.times(1))
                .deleteById(groupId);
        Mockito.verifyNoMoreInteractions(repositoryMock);

    }

    @Test
    void updateById() {

    }

    @Test
    void updateById_resourceNotFound() {
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
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, executable);

    }
}