package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.auth.AuthResponseDTO;
import io.a_caminho.backend.dto.auth.LoginRequestDTO;
import io.a_caminho.backend.model.RefreshToken;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.repository.UserRepository;
import io.a_caminho.backend.security.jwt.JwtTokenProvider;
import io.a_caminho.backend.security.service.RefreshTokenService;
import io.a_caminho.backend.security.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtils cookieUtils;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (user.getPassword() == null || !passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String accessToken = jwtTokenProvider.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Adiciona cookies HttpOnly na resposta
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createAccessTokenCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createRefreshTokenCookie(refreshToken.getToken()).toString());

        return new AuthResponseDTO(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name(),
                "Login realizado com sucesso"
        );
    }

    @Transactional
    public AuthResponseDTO refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenString = cookieUtils.getCookieValue(request, CookieUtils.REFRESH_TOKEN_COOKIE_NAME)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token não encontrado nos cookies"));

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString)
                .map(refreshTokenService::verifyExpirationAndRevocation)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido ou expirado"));

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateToken(user);

        // Atualiza cookie do access token
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createAccessTokenCookie(newAccessToken).toString());

        return new AuthResponseDTO(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name(),
                "Token renovado com sucesso"
        );
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        cookieUtils.getCookieValue(request, CookieUtils.REFRESH_TOKEN_COOKIE_NAME)
                .ifPresent(refreshTokenService::revokeToken);

        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.deleteAccessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.deleteRefreshTokenCookie().toString());
    }
}
