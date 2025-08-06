package school.faang.springsecuritydemo.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.springsecuritydemo.domain.Role;
import school.faang.springsecuritydemo.dto.RoleDto;
import school.faang.springsecuritydemo.mapper.RoleMapper;
import school.faang.springsecuritydemo.repository.RoleRepository;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
    }

    public void createNewRole(RoleDto roleDto) {
        roleRepository.save(roleMapper.toRole(roleDto));
    }
}
