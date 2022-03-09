package it.pagopa.selfcare.user_group.core;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.user_group.connector.api.UserGroupConnector;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupConnector groupConnector;

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
    public void deleteGroup(String id) {
        log.trace("deleteGroup start");
        log.debug("deleteGroup id = {}", id);
        Assert.hasText(id, "A user group id is required");
        groupConnector.deleteById(id);
        log.trace("deleteProduct end");
    }

    @Override
    public void suspendGroup(String id) {
        log.trace("suspendGroup start");
        log.debug("suspendGroup id = {}", id);
        Assert.hasText(id, "A user group id is required");
        groupConnector.suspendById(id);
        log.trace("suspendGroup end");
    }

    @Override
    public void activateGroup(String id) {
        log.trace("activateGroup start");
        log.debug("activateGroup id = {}", id);
        Assert.hasText(id, "A user group id is required");
        groupConnector.activateById(id);
        log.trace("activateGroup end");
    }

    @Override
    public UserGroupOperations updateGroup(String id, UserGroupOperations group) {
        return null;
    }
}
