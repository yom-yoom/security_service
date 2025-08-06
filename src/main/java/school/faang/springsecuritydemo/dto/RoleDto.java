package school.faang.springsecuritydemo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import school.faang.springsecuritydemo.domain.Privilege;

import java.util.Set;

public record RoleDto(

        @NotBlank
        @Min(3)
        String name,

        Set<Privilege> privileges
) {}
