package it.pagopa.selfcare.user_group.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.user_group.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.web.model.CreateUserGroupDto;
import it.pagopa.selfcare.user_group.web.model.UserGroupResource;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupMapperTest {

    @Test
    void toResource_notNull() {
        // given
        OffsetDateTime now = OffsetDateTime.now().minusSeconds(1);
        UserGroupOperations group = TestUtils.mockInstance(new DummyGroupOperations());
        group.setMembers(List.of(UUID.randomUUID()));
        //when
        UserGroupResource resource = GroupMapper.toResource(group);
        //then
        assertEquals(group.getId(), resource.getId());
        assertEquals(group.getInstitutionId(), resource.getInstitutionId());
        assertEquals(group.getProductId(), resource.getProductId());
        assertEquals(group.getDescription(), resource.getDescription());
        assertTrue(now.isBefore(resource.getCreatedAt()));
        assertTrue(now.isBefore(resource.getModifiedAt()));
        assertEquals(group.getMembers(), resource.getMembers());
        assertEquals(group.getName(), resource.getName());
        assertEquals(group.getCreatedBy(), resource.getCreatedBy());
        assertEquals(group.getModifiedBy(), resource.getModifiedBy());
        assertEquals(group.getStatus(), resource.getStatus());
        TestUtils.reflectionEqualsByName(resource, group);
    }

    @Test
    void toResource_null() {
        //given
        UserGroupOperations entity = null;
        //when
        UserGroupResource resource = GroupMapper.toResource(entity);
        //then
        assertNull(resource);
    }

    @Test
    void fromDto_notNull() {
        //given
        CreateUserGroupDto dto = TestUtils.mockInstance(new CreateUserGroupDto());
        dto.setMembers(List.of(UUID.randomUUID()));
        //when
        UserGroupOperations group = GroupMapper.fromDto(dto);
        //then
        assertNotNull(group);
        TestUtils.reflectionEqualsByName(group, dto);
    }

    @Test
    void fromDto_null() {
        //given
        //when
        UserGroupOperations group = GroupMapper.fromDto(null);
        //then
        assertNull(group);
    }
}