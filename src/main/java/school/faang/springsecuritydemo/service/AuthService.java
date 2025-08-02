package school.faang.springsecuritydemo.service;

import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import school.faang.springsecuritydemo.auth.CustomUserDetails;
import school.faang.springsecuritydemo.auth.SecurityConstants;
import school.faang.springsecuritydemo.domain.RefreshToken;
import school.faang.springsecuritydemo.domain.User;
import school.faang.springsecuritydemo.dto.request.LoginRequest;
import school.faang.springsecuritydemo.dto.request.RegistrationUserRequest;
import school.faang.springsecuritydemo.dto.response.JwtResponse;
import school.faang.springsecuritydemo.dto.response.UserResponse;
import school.faang.springsecuritydemo.exception.AppError;
import school.faang.springsecuritydemo.util.JwtTokenUtils;

/**
 * Сервис для аутентификации и управления токенами.
 * <p>
 * Этот сервис отвечает за создание и обновление JWT токенов для пользователей,
 * а также за регистрацию новых пользователей. Он использует различные компоненты,
 * такие как UserService, JwtTokenUtils и RefreshTokenService для выполнения своей работы.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    // Сервис для работы с пользователями
    private final UserService userService;

    // Утилиты для работы с JWT токенами
    private final JwtTokenUtils jwtTokenUtils;

    // Менеджер аутентификации для аутентификации пользователей
    private final AuthenticationManager authenticationManager;

    // Сервис для работы с refresh-токенами
    private final RefreshTokenService refreshTokenService;

    // Константы безопасности, включая секреты для токенов
    private final SecurityConstants securityConstants;

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Создание новых JWT токенов (access и refresh) после успешной аутентификации пользователя.
     * <p>
     * Этот метод аутентифицирует пользователя, генерирует для него новый access токен и refresh токен,
     * а затем сохраняет новый refresh токен в базе данных.
     *
     * @param authRequest запрос с данными для аутентификации (имя пользователя и пароль).
     * @return объект JwtResponse с новым access и refresh токенами.
     */
    public JwtResponse createAuthToken(@RequestBody LoginRequest authRequest) {
        // Аутентификация пользователя
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(),
                        authRequest.getPassword()));

        // Загрузка данных пользователя
        CustomUserDetails userDetails =
                customUserDetailsService.loadUserByUsername(authRequest.getUsername());

        // Генерация токенов
        var accessToken = jwtTokenUtils.generateAccessToken(userDetails);
        var refreshToken = jwtTokenUtils.generateRefreshToken(userDetails);

        // Сохранение refresh токена в базе данных
        refreshTokenService.save(new RefreshToken(refreshToken, userDetails.getId()));

        // Возврат токенов
        return new JwtResponse(accessToken, refreshToken);
    }

    /**
     * Регистрация нового пользователя.
     * <p>
     * Этот метод проверяет, совпадают ли пароли, а также не существует ли уже пользователь с таким именем.
     * Если все проверки проходят, то создается новый пользователь и возвращается информация о нем.
     *
     * @param registrationUserRequest запрос с данными для регистрации пользователя.
     * @return ответ с информацией о зарегистрированном пользователе или ошибкой.
     */
    public ResponseEntity<?> createNewUser(
            @RequestBody RegistrationUserRequest registrationUserRequest) {
        // Проверка совпадения паролей
        if (!registrationUserRequest.getPassword()
                .equals(registrationUserRequest.getConfirmPassword())) {
            return new ResponseEntity<>(
                    new AppError(HttpStatus.BAD_REQUEST.value(), "Пароли не совпадают"),
                    HttpStatus.BAD_REQUEST);
        }

        // Проверка на существование пользователя с таким именем
        if (userService.findByUsername(registrationUserRequest.getUsername()).isPresent()) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(),
                    "Пользователь с указанным именем уже существует"), HttpStatus.BAD_REQUEST);
        }

        // Создание нового пользователя
        User user = userService.createNewUser(registrationUserRequest);

        // Возврат данных о новом пользователе
        return ResponseEntity.ok(
                new UserResponse(user.getId(), user.getUsername(), user.getEmail()));
    }

    /**
     * Обновление токенов (access и refresh) с использованием refresh токена.
     * <p>
     * Этот метод проверяет действительность refresh токена и, если он действителен,
     * генерирует новые access и refresh токены. Затем старый refresh токен удаляется,
     * и новый сохраняется в базе данных.
     *
     * @param oldRefreshToken переданный в header.
     * @return объект JwtResponse с новыми access и refresh токенами.
     * @throws AuthException если переданный refresh токен не действителен.
     */
    @Transactional
    public JwtResponse attemptToRefreshTokens(String oldRefreshToken)
            throws AuthException {

        // Проверка существования refresh токена
        isTokenValid(oldRefreshToken);

        // Загрузка данных пользователя по старому refresh токену
        CustomUserDetails userDetails =
                customUserDetailsService.loadUserByUsername(
                        jwtTokenUtils.getUsername(oldRefreshToken,
                                securityConstants.getRefreshSecret()));

        // Генерация новых токенов
        var accessToken = jwtTokenUtils.generateAccessToken(userDetails);
        var refreshToken = jwtTokenUtils.generateRefreshToken(userDetails);

        // Удаление старого refresh токена и сохранение нового
        refreshTokenService.deleteByToken(oldRefreshToken);
        refreshTokenService.save(new RefreshToken(refreshToken, userDetails.getId()));

        // Возврат новых токенов
        return new JwtResponse(accessToken, refreshToken);
    }

    public void logout(String token) throws AuthException {
        isTokenValid(token);
        refreshTokenService.deleteByToken(token);
    }

    private void isTokenValid(String token) throws AuthException {
        if (!refreshTokenService.existsByToken(token)) {
            throw new AuthException("Переданный refresh-токен не действителен");
        }
    }
}
