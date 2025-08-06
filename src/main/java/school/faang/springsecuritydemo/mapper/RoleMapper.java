package school.faang.springsecuritydemo.mapper;

import org.mapstruct.Mapper;
import school.faang.springsecuritydemo.domain.Role;
import school.faang.springsecuritydemo.dto.RoleDto;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toRole(RoleDto roleDto);
}
