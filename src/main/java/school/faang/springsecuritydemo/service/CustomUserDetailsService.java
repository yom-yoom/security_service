package school.faang.springsecuritydemo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.springsecuritydemo.auth.CustomUserDetails;
import school.faang.springsecuritydemo.domain.User;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    /**
     * Загрузка пользователя по имени пользователя.
     * <p>
     * Этот метод загружает пользователя из базы данных по имени пользователя,
     * а также извлекает его роли и создает объект `CustomUserDetails`, который используется для аутентификации.
     *
     * @param username имя пользователя для поиска.
     * @return объект `CustomUserDetails` с данными о пользователе.
     * @throws UsernameNotFoundException если пользователь с таким именем не найден.
     */
    @Override
    @Transactional
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Поиск пользователя по имени
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Пользователь '%s' не найден", username)));

        // Преобразование ролей пользователя в список SimpleGrantedAuthority
        Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();

        // Возвращаем объект CustomUserDetails
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
