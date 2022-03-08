package it.pagopa.selfcare.user_group.core;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.user_group.connector.DummyGroup;
import it.pagopa.selfcare.user_group.connector.api.UserGroupConnector;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.TestSecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserGroupServiceImplTest {

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }

    @Mock
    private UserGroupConnector groupConnectorMock;


    @InjectMocks
    private UserGroupServiceImpl groupService;

    @Test
    void createGroup_nullAuth() {
        //given
        UserGroupOperations input = null;
        //when
        Executable executable = () -> groupService.createGroup(input);
        //then
        IllegalStateException e = assertThrows(IllegalStateException.class, executable);
        assertEquals("Authentication is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void createGroup_nullPrincipal() {
        //given
        UserGroupOperations input = null;
        Authentication authentication = new TestingAuthenticationToken(null, null);
        TestSecurityContextHolder.setAuthentication(authentication);
        //when
        Executable executable = () -> groupService.createGroup(input);
        //then
        IllegalStateException illegalStateException = Assertions.assertThrows(IllegalStateException.class, executable);
        Assertions.assertEquals("Not SelfCareUser principal", illegalStateException.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void createGroup_nullGroup() {
        //given
        SelfCareUser selfCareUser = SelfCareUser.builder("id")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        UserGroupOperations input = null;
        //when
        Executable executable = () -> groupService.createGroup(input);
        //then
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, executable);
        Assertions.assertEquals("A group is required", illegalArgumentException.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void createGroup_ok() {
        //given
        Instant now = Instant.now().minusSeconds(1);
        SelfCareUser selfCareUser = SelfCareUser.builder("userId")
                .email("test@example.com")
                .name("name")
                .surname("surname")
                .build();
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(selfCareUser, null);
        TestSecurityContextHolder.setAuthentication(authenticationToken);
        List<UUID> members = List.of(UUID.randomUUID(), UUID.randomUUID());
        UserGroupOperations input = TestUtils.mockInstance(new DummyGroup(), "setId", "setCreateAt", "setModifiedAt");
        input.setId("id");
        input.setMembers(members);
        Mockito.when(groupConnectorMock.insert(Mockito.any(UserGroupOperations.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, UserGroupOperations.class));
        //when
        UserGroupOperations output = groupService.createGroup(input);
        //then
        assertNotNull(output);

        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .insert(Mockito.any(UserGroupOperations.class));
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void deleteGroup() {
        //given
        String id = "id";
        //when
        groupService.deleteGroup(id);
        //then
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .deleteById(id);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }


    @Test
    void deleteGroup_nullId() {
        //given
        String id = null;
        //when
        Executable executable = () -> groupService.deleteGroup(id);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A user group id is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void suspendGroup() {
        //given
        String id = "id";
        //when
        groupService.suspendGroup(id);
        //then
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .suspendById(id);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }


    @Test
    void suspendGroup_nullId() {
        //given
        String id = null;
        //when
        Executable executable = () -> groupService.suspendGroup(id);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A user group id is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void activateGroup() {
        //given
        String id = "id";
        //when
        groupService.activateGroup(id);
        //then
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .activateById(id);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void activateGroup_nullId() {
        //given
        String id = null;
        //when
        Executable executable = () -> groupService.activateGroup(id);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A user group id is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

}