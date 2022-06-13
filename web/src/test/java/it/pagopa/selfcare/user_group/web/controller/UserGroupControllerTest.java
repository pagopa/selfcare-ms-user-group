package it.pagopa.selfcare.user_group.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.user_group.core.UserGroupService;
import it.pagopa.selfcare.user_group.web.config.WebTestConfig;
import it.pagopa.selfcare.user_group.web.handler.UserGroupExceptionHandler;
import it.pagopa.selfcare.user_group.web.model.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(value = {UserGroupController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
        UserGroupController.class,
        UserGroupExceptionHandler.class,
        WebTestConfig.class
})
class UserGroupControllerTest {

    private static final DummyCreateUserGroupDto CREATE_USER_GROUP_DTO = TestUtils.mockInstance(new DummyCreateUserGroupDto());
    private static final DummyUpdateUserGroupDto UPDATE_USER_GROUP_DTO = TestUtils.mockInstance(new DummyUpdateUserGroupDto());
    private static final String BASE_URL = "/user-groups/v1";

    @MockBean
    private UserGroupService groupServiceMock;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    void createGroup() throws Exception {
        //given
        Mockito.when(groupServiceMock.createGroup(Mockito.any(UserGroupOperations.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, UserGroupOperations.class));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/")
                .content(mapper.writeValueAsString(CREATE_USER_GROUP_DTO))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn();
        //then
        UserGroupResource group = mapper.readValue(result.getResponse().getContentAsString(), UserGroupResource.class);
        assertNotNull(group);
        TestUtils.reflectionEqualsByName(CREATE_USER_GROUP_DTO, group);
    }

    @Test
    void deleteGroup_doesNotExists() throws Exception {
        //given
        Mockito.doThrow(ResourceNotFoundException.class)
                .when(groupServiceMock).deleteGroup(Mockito.anyString());
        //when
        mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/id")
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(not(emptyString())));
        //then
    }

    @Test
    void deleteGroup() throws Exception {
        Mockito.doNothing()
                .when(groupServiceMock).deleteGroup(Mockito.anyString());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/id")
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        assertEquals("", result.getResponse().getContentAsString());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .deleteGroup(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(groupServiceMock);

    }

    @Test
    void activateGroup_doesNotExists() throws Exception {
        //given
        String groupId = "groupId";
        Mockito.doThrow(ResourceNotFoundException.class)
                .when(groupServiceMock).activateGroup(Mockito.anyString());
        //when
        mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{id}/activate", groupId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(not(emptyString())));
        //then
    }

    @Test
    void activateGroup() throws Exception {
        //given
        String groupId = "groupId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{id}/activate", groupId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .activateGroup(groupId);
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void suspendGroup_doesNotExists() throws Exception {
        //given
        String groupId = "groupId";
        Mockito.doThrow(ResourceNotFoundException.class)
                .when(groupServiceMock).suspendGroup(Mockito.anyString());
        //when
        mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{id}/suspend", groupId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(not(emptyString())));
        //then
    }

    @Test
    void suspendGroup() throws Exception {
        //given
        String groupId = "groupId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{id}/suspend", groupId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .suspendGroup(groupId);
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void updateGroup_exists() throws Exception {
        //given
        Mockito.when(groupServiceMock.updateGroup(Mockito.anyString(), Mockito.any(UserGroupOperations.class)))
                .thenAnswer(invocationOnMock -> {
                    String id = invocationOnMock.getArgument(0, String.class);
                    UserGroupOperations group = invocationOnMock.getArgument(1, UserGroupOperations.class);
                    group.setId(id);
                    group.setMembers(Set.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
                    return group;
                });
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .put(BASE_URL + "/id")
                .content(mapper.writeValueAsString(UPDATE_USER_GROUP_DTO))
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        //then
        UserGroupResource group = mapper.readValue(result.getResponse().getContentAsString(), UserGroupResource.class);

        assertNotNull(group);
        TestUtils.reflectionEqualsByName(UPDATE_USER_GROUP_DTO, group);
    }

    @Test
    void addMember() throws Exception {
        //given
        String groupId = "groupId";
        MemberUUID member = new MemberUUID();
        member.setMember(UUID.randomUUID());
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .put(BASE_URL + "/groupId/members/" + member.getMember())
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .addMember(groupId, member.getMember());
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void deleteMember() throws Exception {
        //given
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/groupId/members/" + memberId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .deleteMember(groupId, memberId.toString());
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

    @Test
    void getUserGroup() throws Exception {
        //given
        String InstitutionId = "institutionId";
        String productId = "productId";
        Mockito.when(groupServiceMock.getUserGroup(Mockito.anyString()))
                .thenAnswer(invocationOnMock -> {
                    String id = invocationOnMock.getArgument(0, String.class);
                    UserGroupOperations group = TestUtils.mockInstance(new GroupDto(), "setId");
                    group.setId(id);
                    group.setMembers(Set.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
                    return group;
                });
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/groupId")
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        //then
        UserGroupResource group = mapper.readValue(result.getResponse().getContentAsString(), UserGroupResource.class);
        assertNotNull(group);
    }

    @Test
    void getUserGroups() throws Exception {
        //given
        String institutionId = "institutionId";
        String productId = "productId";
        UserGroupOperations groupOperations = TestUtils.mockInstance(new GroupDto());
        groupOperations.setMembers(Set.of(UUID.randomUUID().toString()));
        Mockito.when(groupServiceMock.getUserGroups(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.singletonList(groupOperations));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .get(BASE_URL + "/")
                .param("institutionId", institutionId)
                .param("productId", productId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        //then
        List<UserGroupResource> groups = mapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertNotNull(groups);
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .getUserGroups(Mockito.any(), Mockito.any(), Mockito.any(), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertTrue(capturedPageable.getSort().isUnsorted());
    }

    @Test
    void deleteMembers() throws Exception {
        //given
        String groupId = "groupId";
        UUID memberId = UUID.randomUUID();
        String institutionId = "institutionId";
        String productId = "productId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/members/" + memberId)
                .param("institutionId", institutionId)
                .param("productId", productId)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .deleteMembers(memberId.toString(), institutionId, productId);
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

}