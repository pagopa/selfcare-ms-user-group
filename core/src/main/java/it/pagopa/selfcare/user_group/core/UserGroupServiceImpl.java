package it.pagopa.selfcare.user_group.core;

import it.pagopa.selfcare.commons.base.security.SelfCareUser;
import it.pagopa.selfcare.user_group.api.UserGroupConnector;
import it.pagopa.selfcare.user_group.api.UserGroupOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;

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
        SelfCareUser principal = ((SelfCareUser) authentication.getPrincipal());
        Assert.notNull(group, "A group is required");
        Instant now = Instant.now();
        group.setCreatedAt(now);
        group.setCreatedBy(principal.getId());
        group.setModifiedAt(now);
        group.setModifiedBy(principal.getId());
        UserGroupOperations insert = groupConnector.insert(group);
        log.debug("insert = {}", insert);
        log.trace("createGroup end");
        return insert;
    }

    @Override
    public void deleteGroup(String id) {
        log.trace("deleteGroup start");
        log.debug("deleteGroup id = {}", id);
        Assert.notNull(id, "A user group id is required");
        groupConnector.deleteById(id);
        log.trace("deleteProduct end");
    }
}
