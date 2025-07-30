package school.faang.springsecuritydemo.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import school.faang.springsecuritydemo.service.CustomUserDetailsService;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final PasswordEncoder passwordEncoder;

    /**
     * Конфигурирует фильтр безопасности для HTTP-запросов.
     * <p>
     * Отключает CSRF и CORS, настраивает маршруты для публичного доступа,
     * а также указывает, что для всех других запросов требуется аутентификация.
     * Конфигурирует управление сессиями для использования без состояния (stateless),
     * что подходит для приложений, использующих JWT для аутентификации.
     *
     * @param http HttpSecurity, который используется для настройки безопасности.
     * @return настроенный SecurityFilterChain.
     * @throws Exception если возникнут ошибки при конфигурации.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers(this.getIgnoredPaths()) // Игнорируемые маршруты
                        .permitAll() // Публичный доступ
                        .anyRequest()
                        .authenticated()) // Все остальные запросы требуют аутентификации
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Отключение сессий
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))) // Обработка ошибок аутентификации
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // Добавление фильтра для JWT
        return http.build(); // Строим и возвращаем конфигурацию безопасности
    }


    /**
     * Возвращает список путей, которые не требуют аутентификации.
     * <p>
     * Эти маршруты доступны для всех пользователей без авторизации.
     *
     * @return массив строк с путями, которые не требуют аутентификации.
     */
    private String[] getIgnoredPaths() {
        return new String[]{
                "/authorization/login", // Маршрут для логина
                "/authorization/refresh-tokens", // Маршрут для обновления токенов
                "/test/unsecured" // Пример маршрута, который не защищен
        };
    }

    /**
     * Конфигурирует CORS для приложения.
     * <p>
     * CORS-настройки определяют, какие внешние домены могут делать запросы к вашему приложению.
     *
     * @return объект `CorsConfigurationSource`, который настраивает политику CORS.
     */
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000",
                "https://example.com")); // Разрешенные источники (домен и порт)
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE")); // Разрешенные HTTP-методы
        configuration.setAllowedHeaders(
                Arrays.asList("Authorization", "Content-Type")); // Разрешенные заголовки
        configuration.setAllowCredentials(
                true); // Разрешение на отправку куки и заголовков с авторизацией
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Применение CORS для всех путей
        return source;
    }

    /**
     * Конфигурирует DaoAuthenticationProvider для аутентификации пользователей.
     * <p>
     * Этот компонент используется для аутентификации пользователей с использованием
     * базы данных, где пароли хранятся в зашифрованном виде. Устанавливается энкодер
     * паролей и сервис для работы с пользователями.
     *
     * @return настроенный DaoAuthenticationProvider.
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
        return daoAuthenticationProvider;
    }

    /**
     * Конфигурирует AuthenticationManager для управления аутентификацией.
     * <p>
     * AuthenticationManager используется для управления процессом аутентификации.
     *
     * @param authenticationConfiguration конфигурация аутентификации.
     * @return настроенный AuthenticationManager.
     * @throws Exception если возникнут ошибки при настройке.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}


