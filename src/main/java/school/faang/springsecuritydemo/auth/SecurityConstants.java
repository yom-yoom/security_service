package school.faang.springsecuritydemo.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Класс, содержащий константы и настройки безопасности, связанные с JWT.
 * <p>
 * Этот класс используется для хранения конфигурации, связанной с JWT-токенами, такой, как:<br/>
 * - заголовок авторизации,<br/>
 * - префикс "Bearer",<br/>
 * - секретные ключи для доступа и обновления,<br/>
 * - время жизни токенов.<br/>
 * <p>
 * Все значения берутся из конфигурационного файла (например, `application.properties` или `application.yml`)
 * с префиксом `security.jwt`.
 */
@Getter
@Setter
@Component
@Slf4j
@ConfigurationProperties(prefix = "security.jwt")
public class SecurityConstants {

    /**
     * Заголовок, который используется для передачи токена
     */
    private String authHeader;

    /**
     * Префикс, который должен быть добавлен к токену
     */
    private String bearerPrefix;

    /**
     * Секретный ключ для проверки подлинности токена доступа
     */
    private String accessSecret;

    /**
     * Время жизни токена доступа в миллисекундах
     */
    private Integer accessLifetime;

    /**
     * Секретный ключ для проверки подлинности токена обновления
     */
    private String refreshSecret;

    /**
     * Время жизни токена обновления в миллисекундах
     */
    private Integer refreshLifetime;

    private boolean isCookieSecure;
}

