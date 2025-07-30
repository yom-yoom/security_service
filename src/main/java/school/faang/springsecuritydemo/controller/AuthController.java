package school.faang.springsecuritydemo.controller;

import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.springsecuritydemo.auth.SecurityConstants;
import school.faang.springsecuritydemo.dto.request.LoginRequest;
import school.faang.springsecuritydemo.dto.request.RegistrationUserRequest;
import school.faang.springsecuritydemo.dto.response.JwtResponse;
import school.faang.springsecuritydemo.service.AuthService;

@RestController
@RequestMapping("/authorization")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityConstants securityConstants;

    @PostMapping("/login")
    public ResponseEntity<String> createAuthToken(@RequestBody LoginRequest authRequest) {
        JwtResponse response = authService.createAuthToken(authRequest);
        ResponseCookie refreshCookie = createResponseCookie(response);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, securityConstants.getBearerPrefix() + response.getAccessToken())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Login successful");
    }

    @PostMapping("/refresh-tokens")
    public ResponseEntity<String> attemptToRefreshToken(HttpServletRequest request)
            throws AuthException {
        String authHeader = request.getHeader(securityConstants.getAuthHeader());
        String bearerPrefix = securityConstants.getBearerPrefix();

        if (authHeader == null || !authHeader.startsWith(bearerPrefix)) {
            throw new BadCredentialsException("Missing Authorization header");
        }

        String refreshToken = authHeader.substring(bearerPrefix.length());
        JwtResponse response = authService.attemptToRefreshTokens(refreshToken);
        ResponseCookie refreshCookie = createResponseCookie(response);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, securityConstants.getBearerPrefix() + response.getAccessToken())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Token refreshed successfully");
    }

    @PostMapping("/registration")
    public ResponseEntity<?> createNewUser(@RequestBody
                                           RegistrationUserRequest registrationUserRequest) {
        return authService.createNewUser(registrationUserRequest);
    }

    private ResponseCookie createResponseCookie(JwtResponse response) {
        return ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(securityConstants.isCookieSecure())
                .path("/")
                .maxAge(securityConstants.getRefreshLifetime())
                .sameSite("Strict")
                .build();
    }
}
