package school.faang.springsecuritydemo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.faang.springsecuritydemo.dto.RoleDto;
import school.faang.springsecuritydemo.service.RoleService;
import school.faang.springsecuritydemo.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;
    private final UserService userService;

    @PostMapping("/new")
    public void createNewRole(@RequestBody @Valid RoleDto roleDto) {
        roleService.createNewRole(roleDto);
    }

    @PatchMapping("/obtain")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void addRoleOnUser(@RequestParam String roleName, @RequestParam String username) {
        userService.addRoleOnUser(roleName, username);
    }

    @PatchMapping("/loss")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteRoleOnUser(@RequestParam String roleName, @RequestParam String username) {
        userService.deleteRoleOnUser(roleName, username);
    }
}
