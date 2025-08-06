package school.faang.springsecuritydemo.domain;

import lombok.Builder;

@Builder
public record RefreshToken(
        Long userId,
        String token
) {}
