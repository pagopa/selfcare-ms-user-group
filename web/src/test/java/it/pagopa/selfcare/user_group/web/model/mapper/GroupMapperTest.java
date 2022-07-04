package it.pagopa.selfcare.user_group.web.model.mapper;

import it.pagopa.selfcare.commons.utils.TestUtils;
import it.pagopa.selfcare.user_group.connector.api.UserGroupOperations;
import it.pagopa.selfcare.user_group.connector.model.UserGroupFilter;
import it.pagopa.selfcare.user_group.connector.model.UserGroupStatus;
import it.pagopa.selfcare.user_group.web.model.CreateUserGroupDto;
import it.pagopa.selfcare.user_group.web.model.UpdateUserGroupDto;
import it.pagopa.selfcare.user_group.web.model.UserGroupResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GroupMapperTest {

    @Test
    void toResource_notNull() {
        // given
        Instant now = Instant.now().minusSeconds(1);
        UserGroupOperations group = TestUtils.mockInstance(new DummyGroupOperations());
        group.setMembers(Set.of(UUID.randomUUID().toString()));
        //when
        UserGroupResource resource = GroupMapper.toResource(group);
        //then
        assertEquals(group.getId(), resource.getId());
        assertEquals(group.getInstitutionId(), resource.getInstitutionId());
        assertEquals(group.getProductId(), resource.getProductId());
        assertEquals(group.getDescription(), resource.getDescription());
        assertIterableEquals(group.getMembers(), resource.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));
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
    void fromDto_notNullCreate() {
        //given
        CreateUserGroupDto dto = TestUtils.mockInstance(new CreateUserGroupDto());
        dto.setMembers(Set.of(UUID.randomUUID()));
        //when
        UserGroupOperations group = GroupMapper.fromDto(dto);
        //then
        assertNotNull(group);
        TestUtils.reflectionEqualsByName(group, dto);
    }

    @Test
    void fromDto_nullCreate() {
        //given
        CreateUserGroupDto dto = null;
        //when
        UserGroupOperations group = GroupMapper.fromDto(dto);
        //then
        assertNull(group);
    }

    @Test
    void fromDto_notNullUpdate() {
        //given
        UpdateUserGroupDto dto = TestUtils.mockInstance(new UpdateUserGroupDto());
        dto.setMembers(Set.of(UUID.randomUUID()));
        //when
        UserGroupOperations group = GroupMapper.fromDto(dto);
        //then
        assertNotNull(group);
        TestUtils.reflectionEqualsByName(group, dto);
    }

    @Test
    void fromDto_nullUpdate() {
        //given
        UpdateUserGroupDto dto = null;
        //when
        UserGroupOperations group = GroupMapper.fromDto(dto);
        //then
        assertNull(group);
    }

    @Test
    void toFilter() {
        //given
        Optional<String> institutionId = Optional.of("institutionId");
        Optional<String> productId = Optional.of("productId");
        Optional<String> userId = Optional.of("userId");
        Optional<UserGroupStatus> allowedStates = Optional.of(UserGroupStatus.ACTIVE);
        //when
        UserGroupFilter filter = GroupMapper.toFilter(institutionId, productId, userId, allowedStates);
        //then
        assertEquals(institutionId, filter.getInstitutionId());
        assertEquals(productId, filter.getProductId());
        assertEquals(userId, filter.getUserId());
        assertEquals(allowedStates, filter.getStatus());
    }

    @Test
    void toFilter_nullInstitutionId() {
        //given
        Optional<String> institutionId = null;
        Optional<String> productId = Optional.of("productId");
        Optional<String> userId = Optional.of("userId");
        Optional<UserGroupStatus> allowedStates = Optional.of(UserGroupStatus.ACTIVE);
        //when
        Executable executable = () -> GroupMapper.toFilter(institutionId, productId, userId, allowedStates);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Optional institutionId is required", e.getMessage());
    }

    @Test
    void toFilter_nullAllowedStates() {
        //given
        Optional<String> institutionId = Optional.of("institutionId");
        Optional<String> productId = Optional.of("productId");
        Optional<String> userId = Optional.of("userId");
        Optional<UserGroupStatus> allowedStates = null;
        //when
        Executable executable = () -> GroupMapper.toFilter(institutionId, productId, userId, allowedStates);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Optional allowedStatus is required", e.getMessage());
    }

    @Test
    void toFilter_productId() {
        //given
        Optional<String> institutionId = Optional.of("institutionId");
        Optional<String> productId = null;
        Optional<String> userId = Optional.of("userId");
        Optional<UserGroupStatus> allowedStates = Optional.of(UserGroupStatus.ACTIVE);
        //when
        Executable executable = () -> GroupMapper.toFilter(institutionId, productId, userId, allowedStates);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Optional productId is required", e.getMessage());
    }

    @Test
    void toFilter_userId() {
        //given
        Optional<String> institutionId = Optional.of("institutionId");
        Optional<String> productId = Optional.of("productId");
        Optional<String> userId = null;
        Optional<UserGroupStatus> allowedStates = Optional.of(UserGroupStatus.ACTIVE);
        //when
        Executable executable = () -> GroupMapper.toFilter(institutionId, productId, userId, allowedStates);
        //then
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("An Optional userId is required", e.getMessage());
    }

}