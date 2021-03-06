package it.pagopa.selfcare.user_group.web.model.mapper;

import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.web.model.CreateUserGroupDto;
import it.pagopa.selfcare.user_group.web.model.GroupDto;
import it.pagopa.selfcare.user_group.web.model.UpdateUserGroupDto;
import it.pagopa.selfcare.user_group.web.model.UserGroupResource;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class GroupMapper {

    public static UserGroupResource toResource(UserGroupOperations entity) {
        log.trace("toResource start");
        log.debug("toResource entity = {}", entity);

        UserGroupResource resource = null;

        if (entity != null) {
            resource = new UserGroupResource();
            resource.setId(entity.getId());
            resource.setInstitutionId(entity.getInstitutionId());
            resource.setProductId(entity.getProductId());
            resource.setName(entity.getName());
            resource.setDescription(entity.getDescription());
            resource.setMembers(entity.getMembers().stream().map(UUID::fromString).collect(Collectors.toList()));
            resource.setStatus(entity.getStatus());
            resource.setCreatedAt(entity.getCreatedAt());
            resource.setCreatedBy(entity.getCreatedBy());
            resource.setModifiedAt(entity.getModifiedAt());
            resource.setModifiedBy(entity.getModifiedBy());
        }
        log.debug("toResource result = {}", resource);
        log.trace("toResource end");
        return resource;
    }

    public static UserGroupOperations fromDto(CreateUserGroupDto dto) {
        log.trace("fromDto start");
        log.debug("fromDto dto = {}", dto);
        UserGroupOperations group = null;
        if (dto != null) {
            group = new GroupDto();
            group.setInstitutionId(dto.getInstitutionId());
            group.setProductId(dto.getProductId());
            group.setName(dto.getName());
            group.setDescription(dto.getDescription());
            group.setStatus(dto.getStatus());
            group.setMembers(dto.getMembers().stream().map(UUID::toString).collect(Collectors.toSet()));
        }
        log.debug("fromDto group = {}", group);
        log.trace("fromDto end");
        return group;
    }

    public static UserGroupOperations fromDto(UpdateUserGroupDto dto) {
        log.trace("fromDto start");
        log.debug("fromDto dto = {}", dto);
        UserGroupOperations group = null;
        if (dto != null) {
            group = new GroupDto();

            group.setName(dto.getName());
            group.setDescription(dto.getDescription());
            group.setMembers(dto.getMembers().stream().map(UUID::toString).collect(Collectors.toSet()));
        }
        log.debug("fromDto group = {}", group);
        log.trace("fromDto end");
        return group;
    }
}
