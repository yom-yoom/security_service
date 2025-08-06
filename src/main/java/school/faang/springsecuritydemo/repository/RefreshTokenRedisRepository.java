package school.faang.springsecuritydemo.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import school.faang.springsecuritydemo.domain.RefreshToken;
import school.faang.springsecuritydemo.properties.RedisProperties;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private final RedisTemplate<String, RefreshToken> redisTemplate;
    private final RedisProperties redisProperties;

    public void saveToken(RefreshToken token) {
        redisTemplate.opsForValue().set(token.token(), token, redisProperties.ttlDays());
    }

    public void deleteByToken(String token) {
        redisTemplate.delete(token);
    }

    public boolean existsByToken(String token) {
        return redisTemplate.hasKey(token);
    }
}
