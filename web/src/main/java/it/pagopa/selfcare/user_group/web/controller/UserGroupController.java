package it.pagopa.selfcare.user_group.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.pagopa.selfcare.commons.web.model.Page;
import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.commons.web.model.mapper.PageMapper;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.model.UserGroupFilter;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import it.pagopa.selfcare.user_group.core.UserGroupService;
import it.pagopa.selfcare.user_group.web.model.CreateUserGroupDto;
import it.pagopa.selfcare.user_group.web.model.UpdateUserGroupDto;
import it.pagopa.selfcare.user_group.web.model.UserGroupResource;
import it.pagopa.selfcare.user_group.web.model.mapper.GroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/user-groups/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "user-group")
public class UserGroupController {

    private final UserGroupService groupService;


    @Autowired
    public UserGroupController(UserGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.createUserGroup}")
    @ApiResponse(responseCode = "409",
            description = "Conflict",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
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
    @ApiResponse(responseCode = "409",
            description = "Conflict",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            })
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


    @PutMapping(value = "/{id}/members/{memberId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.addMember}")
    public void addMemberToUserGroup(@ApiParam("${swagger.user-group.model.id}")
                                     @PathVariable("id")
                                             String id,
                                     @ApiParam("${swagger.user-group.model.memberId}")
                                     @PathVariable("memberId")
                                             UUID userId) {
        log.trace("addMemberToUserGroup start");
        log.debug("addMemberToUserGroup id = {}", id);
        groupService.addMember(id, userId);
        log.trace("addMemberToUserGroup end");
    }


    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.getUserGroup}")
    public UserGroupResource getUserGroup(@ApiParam("${swagger.user-group.model.id}")
                                          @PathVariable("id")
                                                  String id) {
        log.trace("getUserGroup start");
        log.debug("getUserGroup id = {}", id);
        UserGroupOperations group = groupService.getUserGroup(id);
        UserGroupResource groupResource = GroupMapper.toResource(group);
        log.debug("getUserGroup result = {}", groupResource);
        log.trace("getUserGroup end");
        return groupResource;
    }


    @GetMapping(value = "/")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.getUserGroups}")
    public Page<UserGroupResource> getUserGroups(@ApiParam("${swagger.user-group.model.institutionId}")
                                                 @RequestParam(value = "institutionId", required = false)
                                                         Optional<String> institutionId,
                                                 @ApiParam("${swagger.user-group.model.productId}")
                                                 @RequestParam(value = "productId", required = false)
                                                         Optional<String> productId,
                                                 @ApiParam("${swagger.user-group.model.memberId}")
                                                 @RequestParam(value = "userId", required = false)
                                                         Optional<UUID> memberId,
                                                 @ApiParam("${swagger.user-group.model.statusFilter}")
                                                 @RequestParam(value = "status", required = false)
                                                         Optional<UserGroupStatus> status,
                                                 Pageable pageable) {
        log.trace("getUserGroups start");
        log.debug("getUserGroups institutionId = {}, productId = {}, pageable = {}, status = {}", institutionId, productId, pageable, status);
        UserGroupFilter filter = UserGroupFilter.builder().userId(memberId.map(UUID::toString)).institutionId(institutionId).productId(productId).status(status).build();
        Page<UserGroupResource> result = PageMapper.map(groupService.getUserGroups(filter, pageable)
                .map(GroupMapper::toResource));
        log.debug("getUserGroups result = {}", result);
        log.trace("getUserGroups end");
        return result;
    }

    @DeleteMapping(value = "/{id}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.deleteMember}")
    public void deleteMemberFromUserGroup(@ApiParam("${swagger.user-group.model.id}")
                                          @PathVariable("id")
                                                  String userGroupId,
                                          @ApiParam("${swagger.user-group.model.memberId}")
                                          @PathVariable("memberId")
                                                  UUID memberId) {
        log.trace("deleteMemberFromUserGroup start");
        log.debug("deleteMemberFromUserGroup userGroupId = {}, memberId = {}", userGroupId, memberId);
        groupService.deleteMember(userGroupId, memberId.toString());
        log.trace("deleteMemberFromUserGroup end");
    }


    @DeleteMapping(value = "/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "", notes = "${swagger.user-group.groups.api.deleteMember}")
    public void deleteMemberFromUserGroups(@ApiParam("${swagger.user-group.model.memberId}")
                                           @PathVariable("memberId")
                                                   UUID memberId,
                                           @RequestParam(value = "institutionId") String institutionId,
                                           @RequestParam(value = "productId") String productId) {
        log.trace("deleteMemberFromUserGroups start");
        log.debug("deleteMemberFromUserGroups memberId = {}, institutionId = {}, productId = {}", memberId, institutionId, productId);
        groupService.deleteMembers(memberId.toString(), institutionId, productId);
        log.trace("deleteMemberFromUserGroups end");
    }

}
