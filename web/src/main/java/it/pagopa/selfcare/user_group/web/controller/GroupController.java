package it.pagopa.selfcare.user_group.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.core.UserGroupService;
import it.pagopa.selfcare.user_group.web.model.CreateUserGroupDto;
import it.pagopa.selfcare.user_group.web.model.MemberUUID;
import it.pagopa.selfcare.user_group.web.model.UpdateUserGroupDto;
import it.pagopa.selfcare.user_group.web.model.UserGroupResource;
import it.pagopa.selfcare.user_group.web.model.mapper.GroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/user-groups", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "user-group")
public class GroupController {

    private final UserGroupService groupService;


    @Autowired
    public GroupController(UserGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.createUserGroup}")
    public UserGroupResource createGroup(@RequestBody
                                         @Valid
                                                 CreateUserGroupDto group) {
        log.trace("createGroup start");
        log.debug("createGroup group = {}", group);
        UserGroupOperations groupOperations = groupService.createGroup(GroupMapper.fromDto(group));
        UserGroupResource result = GroupMapper.toResource(groupOperations);
        log.debug("createGroup result = {}", result);
        log.trace("createGroup end");
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.deleteUserGroup}")
    public void deleteGroup(@ApiParam("${swagger.user-group.model.id}")
                            @PathVariable("id")
                                    String id) {
        log.trace("deteleGroup start");
        log.debug("deleteGroup id = {}", id);
        groupService.deleteGroup(id);
        log.trace("deteleGroup end");

    }

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.activateUserGroup}")
    public void activateGroup(@ApiParam("${swagger.user-group.model.id}")
                              @PathVariable("id")
                                      String id) {
        log.trace("activateGroup start");
        log.debug("activateGroup id = {}", id);
        groupService.activateGroup(id);
        log.trace("activateGroup end");
    }

    @PostMapping("/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.suspendUserGroup}")
    public void suspendGroup(@ApiParam("${swagger.user-group.model.id}")
                             @PathVariable("id")
                                     String id) {
        log.trace("suspendGroup start");
        log.debug("suspendGroup id = {}", id);
        groupService.suspendGroup(id);
        log.trace("suspendGroup end");
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.updateUserGroup}")
    public UserGroupResource updateUserGroup(@ApiParam("${swagger.user-group.model.id}")
                                             @PathVariable("id")
                                                     String id,
                                             @RequestBody
                                             @Valid
                                                     UpdateUserGroupDto groupDto) {
        log.trace("updateUserGroup start");
        log.debug("updateUserGroup id = {}", id);
        UserGroupOperations updatedGroup = groupService.updateGroup(id, GroupMapper.fromDto(groupDto));
        UserGroupResource result = GroupMapper.toResource(updatedGroup);
        log.debug("updateUserGroup result = {}", result);
        log.trace("updateUserGroup end");
        return result;
    }

    @PatchMapping(value = "/{id}/members")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.addMember}")
    public void addMemberToUserGroup(@ApiParam("${swagger.user-group.model.id}")
                                     @PathVariable("id")
                                             String id,
                                     @RequestBody
                                     @Valid
                                             MemberUUID member) {
        log.trace("addMemberToUserGroup start");
        log.debug("addMemberToUserGroup id = {}", id);
        groupService.addMember(id, member.getMember());
        log.trace("addMemberToUserGroup end");
    }

}
