package it.pagopa.selfcare.user_group.core;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.user_group.connector.api.UserGroupConnector;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceUpdateException;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupConnector groupConnector;
    private final static String USER_GROUP_ID_REQUIRED_MESSAGE = "A user group id is required";

    @Autowired
    UserGroupServiceImpl(UserGroupConnector groupConnector) {
        this.groupConnector = groupConnector;
    }

    @Override
    public UserGroupOperations createGroup(UserGroupOperations group) {
        log.trace("createGroup start");
        log.debug("createGroup group = {}", group);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.state(authentication != null, "Authentication is required");
        Assert.state(authentication.getPrincipal() instanceof SelfCareUser, "Not SelfCareUser principal");
        Assert.notNull(group, "A group is required");

        UserGroupOperations insert = groupConnector.insert(group);
        log.debug("insert = {}", insert);
        log.trace("createGroup end");
        return insert;
    }


    @Override
    public void addMember(String id, UUID memberId) {
        log.trace("addMember start");
        log.debug("addMember id = {}, memberId ={}", id, memberId);
        Assert.hasText(id, USER_GROUP_ID_REQUIRED_MESSAGE);
        Assert.notNull(memberId, "A member id is required");
        UserGroupOperations foundGroup = groupConnector.findById(id).orElseThrow(ResourceNotFoundException::new);
        if (UserGroupStatus.SUSPENDED.equals(foundGroup.getStatus())) {
            throw new ResourceUpdateException("Trying to modify suspended group");
        }
        groupConnector.insertMember(id, memberId.toString());
        log.trace("addMember end");
    }

    @Override
    public void deleteMember(String groupId, UUID memberId) {
        log.trace("deleteMember start");
        log.debug("deleteMember groupId = {}, memberId = {}", groupId, memberId);
        Assert.hasText(groupId, USER_GROUP_ID_REQUIRED_MESSAGE);
        Assert.notNull(memberId, "A member id is required");
        UserGroupOperations foundGroup = groupConnector.findById(groupId).orElseThrow(ResourceNotFoundException::new);
        if (UserGroupStatus.SUSPENDED.equals(foundGroup.getStatus())) {
            throw new ResourceUpdateException("Trying to modify suspended group");
        }
        groupConnector.deleteMember(groupId, memberId.toString());
        log.trace("deleteMember end");
    }

    @Override
    public UserGroupOperations getUserGroup(String id) {
        log.trace("getUserGroup start");
        log.debug("getUserGroup id = {}", id);
        Assert.hasText(id, USER_GROUP_ID_REQUIRED_MESSAGE);
        UserGroupOperations foundGroup = groupConnector.findById(id).orElseThrow(ResourceNotFoundException::new);
        log.debug("getUserGroup result = {}", foundGroup);
        log.trace("getUserGroup end");

        return foundGroup;
    }

    @Override
    public List<UserGroupOperations> getUserGroupByInstitutionAndProduct(String institutionId, String productId, Pageable pageable) {
        log.trace("getUserGroup start");
        log.debug("getUserGroup institutionId = {}, productId = {}", institutionId, productId);
        Assert.hasText(institutionId, "An institutionId is required");
        Assert.hasText(productId, "An productId is required");
        List<UserGroupOperations> foundProduct = groupConnector.findByInstitutionIdAndProductId(institutionId, productId, pageable);
        log.debug("getUserGroup result = {}", foundProduct);
        log.trace("getUserGroup end");

        return foundProduct;
    }

    @Override
    public void deleteGroup(String id) {
        log.trace("deleteGroup start");
        log.debug("deleteGroup id = {}", id);
        Assert.hasText(id, USER_GROUP_ID_REQUIRED_MESSAGE);
        groupConnector.deleteById(id);
        log.trace("deleteProduct end");
    }

    @Override
    public void suspendGroup(String id) {
        log.trace("suspendGroup start");
        log.debug("suspendGroup id = {}", id);
        Assert.hasText(id, USER_GROUP_ID_REQUIRED_MESSAGE);
        groupConnector.suspendById(id);
        log.trace("suspendGroup end");
    }

    @Override
    public void activateGroup(String id) {
        log.trace("activateGroup start");
        log.debug("activateGroup id = {}", id);
        Assert.hasText(id, USER_GROUP_ID_REQUIRED_MESSAGE);
        groupConnector.activateById(id);
        log.trace("activateGroup end");
    }

    @Override
    public UserGroupOperations updateGroup(String id, UserGroupOperations group) {
        log.trace("updateGroup start");
        log.debug("updateGroup id = {}, group = {}", id, group);
        Assert.hasText(id, USER_GROUP_ID_REQUIRED_MESSAGE);
        Assert.notNull(group, "A user group is required");
        UserGroupOperations foundGroup = groupConnector.findById(id).orElseThrow(ResourceNotFoundException::new);
        if (UserGroupStatus.SUSPENDED.equals(foundGroup.getStatus())) {
            throw new ResourceUpdateException("Trying to modify suspended group");
        }
        foundGroup.setMembers(group.getMembers());
        foundGroup.setName(group.getName());
        foundGroup.setDescription(group.getDescription());
        UserGroupOperations updatedGroup = groupConnector.save(foundGroup);
        log.debug("updateGroup updatedGroup = {}", updatedGroup);
        log.trace("updateGroup end");
        return updatedGroup;
    }
}
