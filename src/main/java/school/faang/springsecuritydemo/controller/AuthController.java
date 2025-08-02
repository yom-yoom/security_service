package school.faang.springsecuritydemo.controller;

import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) throws AuthException {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        Cookie clearCookie = new Cookie("refreshToken", null);
        clearCookie.setHttpOnly(true);
        clearCookie.setSecure(true);
        clearCookie.setPath("/");
        clearCookie.setMaxAge(0);
        response.addCookie(clearCookie);

        return ResponseEntity.ok().body("Logout successful");
    }

    @PostMapping("/refresh-tokens")
    public ResponseEntity<String> attemptToRefreshToken(HttpServletRequest request) throws AuthException {
        String refreshToken = checkAuthorization(request);
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

    private String checkAuthorization(HttpServletRequest request) {
        String authHeader = request.getHeader(securityConstants.getAuthHeader());
        String bearerPrefix = securityConstants.getBearerPrefix();
        if (authHeader == null || !authHeader.startsWith(bearerPrefix)) {
            throw new BadCredentialsException("Missing Authorization header");
        }
        return authHeader.substring(bearerPrefix.length());
    }
}
