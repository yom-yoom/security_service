package school.faang.springsecuritydemo.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.springsecuritydemo.domain.Role;
import school.faang.springsecuritydemo.domain.User;
import school.faang.springsecuritydemo.dto.request.RegistrationUserRequest;
import school.faang.springsecuritydemo.dto.response.CurrentUserResponse;
import school.faang.springsecuritydemo.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

/**
 * Сервис для работы с пользователями.
 * <p>
 * Этот сервис реализует интерфейс `UserDetailsService`, предоставляя метод для загрузки
 * пользователя по имени пользователя, а также методы для регистрации новых пользователей
 * и получения информации о текущем пользователе.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DEFAULT_USER_ROLE = "ROLE_USER";

    // Репозиторий для работы с пользователями
    private final UserRepository userRepository;

    // Сервис для работы с ролями пользователей
    private final RoleService roleService;

    // Кодировщик паролей для безопасного хранения паролей
    private final PasswordEncoder passwordEncoder;

    /**
     * Поиск пользователя по имени.
     * <p>
     * Этот метод возвращает пользователя, если он существует в базе данных,
     * или `Optional.empty()` в случае отсутствия пользователя.
     *
     * @param username имя пользователя для поиска.
     * @return `Optional<User>` с найденным пользователем или пустым значением, если пользователь не найден.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Регистрация нового пользователя.
     * <p>
     * Этот метод создает нового пользователя, кодирует его пароль с использованием
     * `PasswordEncoder`, присваивает ему роль пользователя и сохраняет в базе данных.
     *
     * @param registrationUserRequest данные для регистрации нового пользователя.
     * @return сохраненный объект пользователя.
     */
    public User createNewUser(RegistrationUserRequest registrationUserRequest) {
        User user = new User();
        user.setUsername(registrationUserRequest.getUsername());
        user.setEmail(registrationUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(
                registrationUserRequest.getPassword()));  // Кодирование пароля
        user.setRoles(Set.of(roleService.getRoleByName(DEFAULT_USER_ROLE)));  // Установка роли пользователя
        return userRepository.save(user);  // Сохранение пользователя в базу данных
    }

    /**
     * Получение информации о текущем пользователе.
     * <p>
     * Этот метод извлекает текущего аутентифицированного пользователя из контекста безопасности
     * и возвращает информацию о нем, такую как ID и имя пользователя.
     *
     * @return объект `CurrentUserResponse` с информацией о текущем пользователе.
     * @throws BadCredentialsException если не удается найти пользователя по имени.
     */
    public CurrentUserResponse getCurrentUserInfo() {
        // Получение текущего аутентифицированного пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Поиск пользователя по имени из контекста аутентификации
        var user = userRepository.findByUsername(authentication.getName()).orElseThrow(
                () -> new BadCredentialsException("Не удалось найти пользователя по имени"));

        // Возвращение данных о текущем пользователе
        return new CurrentUserResponse(user.getId(), user.getUsername());
    }

    @Transactional
    public void addRoleOnUser(String roleName, String username) {
        Role role = roleService.getRoleByName(roleName);
        User user = getUserByUsername(username);
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Transactional
    public void deleteRoleOnUser(String roleName, String username) {
        User user = getUserByUsername(username);
        user.getRoles()
                .removeIf(role -> role.getName().equals(roleName));
        userRepository.save(user);
    }

    private User getUserByUsername(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}

