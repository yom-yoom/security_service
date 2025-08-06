package school.faang.springsecuritydemo.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.redis")
public record RedisProperties(
        String host,
        int port,
        int ttlDays
) {}
