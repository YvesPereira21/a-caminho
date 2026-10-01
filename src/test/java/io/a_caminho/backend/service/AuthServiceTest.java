package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.auth.AuthResponseDTO;
import io.a_caminho.backend.dto.auth.LoginRequestDTO;
import io.a_caminho.backend.model.RefreshToken;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.UserRepository;
import io.a_caminho.backend.security.jwt.JwtTokenProvider;
import io.a_caminho.backend.security.service.RefreshTokenService;
import io.a_caminho.backend.security.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private CookieUtils cookieUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(UUID.randomUUID())
                .email("user@test.com")
                .password("encoded_password")
                .role(UserRole.STUDENT)
                .build();

        refreshToken = RefreshToken.builder()
                .tokenId(UUID.randomUUID())
                .token("valid-refresh-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Deve realizar login com sucesso e setar cookies HttpOnly")
    void shouldLoginSuccessfully() {
        LoginRequestDTO requestDTO = new LoginRequestDTO("user@test.com", "password123");

        when(userRepository.findByEmail(requestDTO.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(requestDTO.password(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("access-token-jwt");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);
        when(cookieUtils.createAccessTokenCookie("access-token-jwt")).thenReturn(ResponseCookie.from("access_token", "access-token-jwt").build());
        when(cookieUtils.createRefreshTokenCookie(refreshToken.getToken())).thenReturn(ResponseCookie.from("refresh_token", refreshToken.getToken()).build());

        AuthResponseDTO responseDTO = authService.login(requestDTO, response);

        assertNotNull(responseDTO);
        assertEquals(user.getEmail(), responseDTO.email());
        assertEquals("STUDENT", responseDTO.role());
        verify(response, times(2)).addHeader(eq("Set-Cookie"), any());
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException se a senha for incorreta")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        LoginRequestDTO requestDTO = new LoginRequestDTO("user@test.com", "wrongpassword");

        when(userRepository.findByEmail(requestDTO.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(requestDTO.password(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(requestDTO, response));
    }

    @Test
    @DisplayName("Deve renovar Access Token com Refresh Token válido")
    void shouldRefreshAccessTokenSuccessfully() {
        when(cookieUtils.getCookieValue(request, CookieUtils.REFRESH_TOKEN_COOKIE_NAME))
                .thenReturn(Optional.of("valid-refresh-token"));
        when(refreshTokenService.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpirationAndRevocation(refreshToken)).thenReturn(refreshToken);
        when(jwtTokenProvider.generateToken(user)).thenReturn("new-access-token");
        when(cookieUtils.createAccessTokenCookie("new-access-token"))
                .thenReturn(ResponseCookie.from("access_token", "new-access-token").build());

        AuthResponseDTO result = authService.refreshAccessToken(request, response);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.email());
        verify(response, times(1)).addHeader(eq("Set-Cookie"), any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar renovar com Refresh Token revogado")
    void shouldThrowExceptionWhenRefreshTokenIsRevoked() {
        refreshToken.setRevoked(true);
        when(cookieUtils.getCookieValue(request, CookieUtils.REFRESH_TOKEN_COOKIE_NAME))
                .thenReturn(Optional.of("revoked-token"));
        when(refreshTokenService.findByToken("revoked-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpirationAndRevocation(refreshToken))
                .thenThrow(new IllegalArgumentException("Refresh token revogado"));

        assertThrows(IllegalArgumentException.class, () -> authService.refreshAccessToken(request, response));
    }

    @Test
    @DisplayName("Deve realizar logout, revogar token e zerar cookies")
    void shouldLogoutSuccessfully() {
        when(cookieUtils.getCookieValue(request, CookieUtils.REFRESH_TOKEN_COOKIE_NAME))
                .thenReturn(Optional.of("token-to-revoke"));
        when(cookieUtils.deleteAccessTokenCookie()).thenReturn(ResponseCookie.from("access_token", "").maxAge(0).build());
        when(cookieUtils.deleteRefreshTokenCookie()).thenReturn(ResponseCookie.from("refresh_token", "").maxAge(0).build());

        authService.logout(request, response);

        verify(refreshTokenService).revokeToken("token-to-revoke");
        verify(response, times(2)).addHeader(eq("Set-Cookie"), any());
    }
}
