package it.pagopa.selfcare.user_group.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.commons.web.model.ErrorResource;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.user_group.core.UserGroupService;
import it.pagopa.selfcare.user_group.web.config.WebTestConfig;
import it.pagopa.selfcare.user_group.web.handler.GroupExceptionHandler;
import it.pagopa.selfcare.user_group.web.model.DummyCreateUserGroupDto;
import it.pagopa.selfcare.user_group.web.model.UserGroupResource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@WebMvcTest(value = {GroupController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
        GroupController.class,
        GroupExceptionHandler.class,
        WebTestConfig.class
})
class GroupControllerTest {

    private static final DummyCreateUserGroupDto CREATE_USER_GROUP_DTO = TestUtils.mockInstance(new DummyCreateUserGroupDto());
    private static final String BASE_URL = "/user-groups";

    @MockBean
    private UserGroupService groupServiceMock;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @Test
    void createGroup() throws Exception {
        //given
        Mockito.when(groupServiceMock.createGroup(Mockito.any(UserGroupOperations.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, UserGroupOperations.class));
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/")
                .content(mapper.writeValueAsString(CREATE_USER_GROUP_DTO))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isCreated())
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
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/id")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();
        //then
        ErrorResource error = mapper.readValue(result.getResponse().getContentAsString(), ErrorResource.class);
        assertNotNull(error);
    }

    @Test
    void deleteGroup() throws Exception {
        Mockito.doNothing()
                .when(groupServiceMock).deleteGroup(Mockito.anyString());
        // when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .delete(BASE_URL + "/id")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
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
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{id}/activate", groupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();
        //then
        ErrorResource error = mapper.readValue(result.getResponse().getContentAsString(), ErrorResource.class);
        assertNotNull(error);
    }

    @Test
    void activateGroup() throws Exception {
        //given
        String groupId = "groupId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{id}/activate", groupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
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
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{id}/suspend", groupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();
        //then
        ErrorResource error = mapper.readValue(result.getResponse().getContentAsString(), ErrorResource.class);
        assertNotNull(error);
    }

    @Test
    void suspendGroup() throws Exception {
        //given
        String groupId = "groupId";
        //when
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post(BASE_URL + "/{id}/suspend", groupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();
        //then
        assertEquals(0, result.getResponse().getContentLength());
        Mockito.verify(groupServiceMock, Mockito.times(1))
                .suspendGroup(groupId);
        Mockito.verifyNoMoreInteractions(groupServiceMock);
    }

}