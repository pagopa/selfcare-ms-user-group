package it.pagopa.selfcare.user_group.core;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.user_group.connector.DummyGroup;
import it.pagopa.selfcare.user_group.connector.api.UserGroupConnector;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceUpdateException;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import it.pagopa.selfcare.user_group.core.config.CoreTestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ValidationException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserGroupServiceImpl.class, CoreTestConfig.class})
@TestPropertySource(properties = {
        "ALLOWED_SORTING_PARAMETERS=name"
})
class UserGroupServiceImplTest {

    @BeforeEach
    void beforeEach() {
        TestSecurityContextHolder.clearContext();
    }

    @MockBean
    private UserGroupConnector groupConnectorMock;

    @Autowired
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
        Set<UUID> members = Set.of(UUID.randomUUID(), UUID.randomUUID());
        UserGroupOperations input = TestUtils.mockInstance(new DummyGroup(), "setId", "setCreateAt", "setModifiedAt");
        input.setId("id");
        input.setMembers(members.stream().map(UUID::toString).collect(Collectors.toSet()));
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

    @Test
    void updateGroup_nullId() {
        //given
        String id = null;
        UserGroupOperations input = new DummyGroup();
        //when
        Executable executable = () -> groupService.updateGroup(id, input);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A user group id is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void updateGroup_nullGroup() {
        //given
        String id = "id";
        UserGroupOperations input = null;
        //when
        Executable executable = () -> groupService.updateGroup(id, input);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A user group is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void updateGroup_foundGroupSuspended() {
        //given
        String id = "id";
        UserGroupOperations group = TestUtils.mockInstance(new DummyGroup());
        group.setStatus(UserGroupStatus.SUSPENDED);
        Mockito.when(groupConnectorMock.findById(Mockito.anyString()))
                .thenReturn(Optional.of(group));
        //when
        Executable executable = () -> groupService.updateGroup(id, group);
        //then
        ResourceUpdateException e = assertThrows(ResourceUpdateException.class, executable);
        assertEquals("Trying to modify suspended group", e.getMessage());
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(id);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void updateGroup_notExists() {
        //given
        String id = "id";
        UserGroupOperations input = new DummyGroup();
        //when
        Executable executable = () -> groupService.updateGroup(id, input);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(id);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void updateGroup_exists() {
        //given
        String id = "id";
        UserGroupOperations group = TestUtils.mockInstance(new DummyGroup(), "setId");
        UserGroupOperations foundGroup = TestUtils.mockInstance(new DummyGroup());
        Mockito.when(groupConnectorMock.findById(Mockito.anyString()))
                .thenReturn(Optional.of(foundGroup));
        Mockito.when(groupConnectorMock.save(Mockito.any()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, UserGroupOperations.class));
        //when
        UserGroupOperations saved = groupService.updateGroup(id, group);
        //then
        assertEquals(saved.getDescription(), group.getDescription());
        assertEquals(saved.getMembers(), group.getMembers());
        assertEquals(saved.getName(), group.getName());
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(id);
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .save(Mockito.any());
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void addMember_nullId() {
        //given
        String id = null;
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> groupService.addMember(id, memberId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A user group id is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void addMember_nullMemberId() {
        //given
        String id = "id";
        UUID memberId = null;
        //when
        Executable executable = () -> groupService.addMember(id, memberId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A member id is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void addMember_doesNotExist() {
        //given
        String id = "id";
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> groupService.addMember(id, memberId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(id);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);

    }

    @Test
    void addMember_groupSuspended() {
        //given
        String id = "id";
        UUID memberId = UUID.randomUUID();
        UserGroupOperations group = TestUtils.mockInstance(new DummyGroup());
        group.setStatus(UserGroupStatus.SUSPENDED);
        Mockito.when(groupConnectorMock.findById(Mockito.anyString()))
                .thenReturn(Optional.of(group));
        //when
        Executable executable = () -> groupService.addMember(id, memberId);
        //then
        ResourceUpdateException e = assertThrows(ResourceUpdateException.class, executable);
        assertEquals("Trying to modify suspended group", e.getMessage());
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(id);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void addMember() {
        //given
        String id = "id";
        UUID memberUUID = UUID.randomUUID();
        UserGroupOperations group = TestUtils.mockInstance(new DummyGroup());
        Mockito.when(groupConnectorMock.findById(Mockito.anyString()))
                .thenReturn(Optional.of(group));
        //when
        groupService.addMember(id, memberUUID);
        //then
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(id);
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .insertMember(Mockito.anyString(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void deleteMember_nullId() {
        //given
        String id = null;
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> groupService.deleteMember(id, memberId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A user group id is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void deleteMember_nullMemberId() {
        //given
        String id = "id";
        UUID memberId = null;
        //when
        Executable executable = () -> groupService.deleteMember(id, memberId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A member id is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void deleteMember_doesNotExist() {
        //given
        String id = "id";
        UUID memberId = UUID.randomUUID();
        //when
        Executable executable = () -> groupService.deleteMember(id, memberId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(id);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);

    }

    @Test
    void deleteMember_groupSuspended() {
        //given
        String id = "id";
        UUID memberId = UUID.randomUUID();
        UserGroupOperations group = TestUtils.mockInstance(new DummyGroup());
        group.setStatus(UserGroupStatus.SUSPENDED);
        Mockito.when(groupConnectorMock.findById(Mockito.anyString()))
                .thenReturn(Optional.of(group));
        //when
        Executable executable = () -> groupService.deleteMember(id, memberId);
        //then
        ResourceUpdateException e = assertThrows(ResourceUpdateException.class, executable);
        assertEquals("Trying to modify suspended group", e.getMessage());
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(id);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void deleteMember() {
        //given
        String id = "id";
        UUID memberUUID = UUID.randomUUID();
        UserGroupOperations group = TestUtils.mockInstance(new DummyGroup());
        Mockito.when(groupConnectorMock.findById(Mockito.anyString()))
                .thenReturn(Optional.of(group));
        //when
        groupService.deleteMember(id, memberUUID);
        //then
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(id);
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .deleteMember(Mockito.anyString(), Mockito.anyString());
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void getGroup() {
        //given
        String groupId = "groupId";
        Mockito.when(groupConnectorMock.findById(Mockito.anyString()))
                .thenAnswer(invocation -> Optional.of(new DummyGroup()));
        //when
        UserGroupOperations group = groupService.getUserGroup(groupId);
        //then
        assertNotNull(group);
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(groupId);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void getGroup_null() {
        //given
        String groupId = "groupId";
        //when
        Executable executable = () -> groupService.getUserGroup(groupId);
        //then
        assertThrows(ResourceNotFoundException.class, executable);
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findById(groupId);
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void getGroup_nullId() {
        //given
        String groupId = null;
        //when
        Executable executable = () -> groupService.getUserGroup(groupId);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("A user group id is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void getUserGroups() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 3, Sort.by("name"));
        Mockito.when(groupConnectorMock.findAll(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(new DummyGroup()));
        //when
        List<UserGroupOperations> groups = groupService.getUserGroups(Optional.of(institutionId), Optional.of(productId), Optional.of(userId), pageable);
        //then
        assertEquals(1, groups.size());
        Mockito.verify(groupConnectorMock, Mockito.times(1))
                .findAll(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(groupConnectorMock);
    }

    @Test
    void getUserGroups_invalidSortParams() {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 3, Sort.by("description"));
        Mockito.when(groupConnectorMock.findAll(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(new DummyGroup()));
        //when
        Executable executable = () -> groupService.getUserGroups(Optional.of(institutionId), Optional.of(productId), Optional.of(userId), pageable);
        ;
        //then
        ValidationException e = assertThrows(ValidationException.class, executable);
        assertEquals("Given sort parameters aren't valid", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void getUserGroups_nullInstitutionId() {
        //given
        Optional<String> institutionId = null;
        Optional<String> productId = Optional.of("productId");
        Optional<UUID> userId = Optional.of(UUID.randomUUID());
        Pageable pageable = PageRequest.of(0, 3, Sort.by("description"));
        Mockito.when(groupConnectorMock.findAll(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(new DummyGroup()));
        //when
        Executable executable = () -> groupService.getUserGroups(institutionId, productId, userId, pageable);
        ;
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Optional institutionId is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void getUserGroups_nullProductId() {
        //given
        Optional<String> institutionId = Optional.of("institutionID");
        Optional<String> productId = null;
        Optional<UUID> userId = Optional.of(UUID.randomUUID());
        Pageable pageable = PageRequest.of(0, 3, Sort.by("description"));
        Mockito.when(groupConnectorMock.findAll(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(new DummyGroup()));
        //when
        Executable executable = () -> groupService.getUserGroups(institutionId, productId, userId, pageable);
        ;
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Optional productId is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

    @Test
    void getUserGroups_nullUserId() {
        //given
        Optional<String> institutionId = Optional.of("institutionID");
        Optional<String> productId = Optional.of("productId");
        Optional<UUID> userId = null;
        Pageable pageable = PageRequest.of(0, 3, Sort.by("description"));
        Mockito.when(groupConnectorMock.findAll(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(new DummyGroup()));
        //when
        Executable executable = () -> groupService.getUserGroups(institutionId, productId, userId, pageable);
        ;
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Optional userId is required", e.getMessage());
        Mockito.verifyNoInteractions(groupConnectorMock);
    }

}