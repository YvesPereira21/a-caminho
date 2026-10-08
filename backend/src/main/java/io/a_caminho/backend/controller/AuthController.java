package io.a_caminho.backend.controller;

import io.a_caminho.backend.controller.openapi.AuthControllerOpenApi;
import io.a_caminho.backend.dto.auth.UserDTO;
import io.a_caminho.backend.dto.auth.UserLoginDTO;
import io.a_caminho.backend.mapper.UserMapper;
import io.a_caminho.backend.model.RefreshToken;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.repository.RefreshTokenRepository;
import io.a_caminho.backend.repository.UserRepository;
import io.a_caminho.backend.security.jwt.JwtTokenProvider;
import io.a_caminho.backend.security.service.RefreshTokenService;
import io.a_caminho.backend.security.util.CookieUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerOpenApi {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtils cookieUtils;
    private final UserMapper userMapper;

    @Override
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        log.info("Authentication attempt for email: {}", loginDTO.email());
        User user = userRepository.findByEmail(loginDTO.email())
                .orElse(null);

        if (user == null || user.getPassword() == null || !passwordEncoder.matches(loginDTO.password(), user.getPassword())) {
            log.warn("Authentication failed for email: {}", loginDTO.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "E-mail ou senha incorretos."));
        }

        log.info("User authenticated successfully: {}", user.getEmail());

        String accessToken = tokenService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(refreshToken.getToken(), refreshTokenService.getRefreshTokenDurationSeconds());

        UserDTO userDTO = userMapper.toDTO(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                        "accessToken", accessToken,
                        "user", userDTO
                ));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = CookieUtils.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenStr) {
        log.info("Token refresh request received");

        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            log.warn("Token refresh rejected: missing cookie");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh Token ausente. Faça login novamente."));
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .orElse(null);

        if (refreshToken == null) {
            log.warn("Token refresh rejected: token invalid or expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh Token inválido ou revogado."));
        }

        User user = refreshToken.getUser();
        log.info("Token refreshed successfully for user: {}", user.getEmail());

        refreshTokenService.deleteByToken(refreshTokenStr);

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        String newAccessToken = tokenService.generateToken(user);
        ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(newRefreshToken.getToken(), refreshTokenService.getRefreshTokenDurationSeconds());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("accessToken", newAccessToken));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = CookieUtils.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenStr) {
        log.info("Logout requested");

        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenService.deleteByToken(refreshTokenStr);
        }

        ResponseCookie cleanCookie = cookieUtils.cleanRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(Map.of("message", "Logout realizado com sucesso."));
    }
}
