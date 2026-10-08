package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.auth.UserDTO;
import io.a_caminho.backend.dto.auth.UserLoginDTO;
import io.a_caminho.backend.mapper.UserMapper;
import io.a_caminho.backend.model.RefreshToken;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.RefreshTokenRepository;
import io.a_caminho.backend.repository.UserRepository;
import io.a_caminho.backend.security.jwt.JwtTokenProvider;
import io.a_caminho.backend.security.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração/Controller - AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtTokenProvider tokenService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private UserMapper userMapper;

    @Nested
    @DisplayName("Cenários Felizes (Happy Path)")
    class HappyPathTests {

        @Test
        @DisplayName("POST /api/auth/login - Login realizado com sucesso retornando token e dados do usuário")
        void shouldReturnOkOnSuccessfulLogin() throws Exception {
            UserLoginDTO request = new UserLoginDTO("aluno@ufpb.br", "senha123");

            User user = User.builder()
                    .userId(UUID.randomUUID())
                    .email("aluno@ufpb.br")
                    .password("encoded_pass")
                    .role(UserRole.STUDENT)
                    .build();

            RefreshToken refreshToken = RefreshToken.builder()
                    .tokenId(UUID.randomUUID())
                    .user(user)
                    .token("sample-refresh-token")
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .build();

            when(userRepository.findByEmail("aluno@ufpb.br")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha123", "encoded_pass")).thenReturn(true);
            when(tokenService.generateToken(user)).thenReturn("sample-access-token");
            when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);
            when(refreshTokenService.getRefreshTokenDurationSeconds()).thenReturn(604800L);
            when(userMapper.toDTO(user)).thenReturn(new UserDTO(user.getUserId(), "Aluno Teste", "aluno@ufpb.br", UserRole.STUDENT));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=sample-refresh-token")))
                    .andExpect(jsonPath("$.accessToken").value("sample-access-token"))
                    .andExpect(jsonPath("$.user.email").value("aluno@ufpb.br"))
                    .andExpect(jsonPath("$.user.role").value("Estudante"));
        }

        @Test
        @DisplayName("POST /api/auth/refresh - Renovação com rotação de refresh token com sucesso")
        void shouldRefreshAccessTokenWithRotation() throws Exception {
            String oldToken = "old-refresh-token";
            String newToken = "new-refresh-token";

            User user = User.builder()
                    .userId(UUID.randomUUID())
                    .email("aluno@ufpb.br")
                    .role(UserRole.STUDENT)
                    .build();

            RefreshToken oldRefreshToken = RefreshToken.builder()
                    .tokenId(UUID.randomUUID())
                    .user(user)
                    .token(oldToken)
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .build();

            RefreshToken newRefreshToken = RefreshToken.builder()
                    .tokenId(UUID.randomUUID())
                    .user(user)
                    .token(newToken)
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .build();

            when(refreshTokenRepository.findByToken(oldToken)).thenReturn(Optional.of(oldRefreshToken));
            when(refreshTokenService.verifyExpiration(oldRefreshToken)).thenReturn(oldRefreshToken);
            when(refreshTokenService.createRefreshToken(user)).thenReturn(newRefreshToken);
            when(tokenService.generateToken(user)).thenReturn("new-access-token");
            when(refreshTokenService.getRefreshTokenDurationSeconds()).thenReturn(604800L);

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refresh_token", oldToken)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=" + newToken)))
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"));

            verify(refreshTokenService).deleteByToken(oldToken);
        }

        @Test
        @DisplayName("POST /api/auth/logout - Logout realizado com sucesso e cookie limpo")
        void shouldLogoutSuccessfully() throws Exception {
            String tokenStr = "active-refresh-token";

            mockMvc.perform(post("/api/auth/logout")
                            .cookie(new Cookie("refresh_token", tokenStr)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                    .andExpect(jsonPath("$.message").value("Logout realizado com sucesso."));

            verify(refreshTokenService).deleteByToken(eq(tokenStr));
        }
    }

    @Nested
    @DisplayName("Cenários de Validação e Erros de Negócio (Sad Path)")
    class BusinessValidationSadPathTests {

        @Test
        @DisplayName("POST /api/auth/login - Credenciais Inválidas retorna 401 Unauthorized")
        void shouldReturnUnauthorizedOnInvalidCredentials() throws Exception {
            UserLoginDTO request = new UserLoginDTO("aluno@ufpb.br", "senha_errada");

            User user = User.builder()
                    .userId(UUID.randomUUID())
                    .email("aluno@ufpb.br")
                    .password("encoded_pass")
                    .role(UserRole.STUDENT)
                    .build();

            when(userRepository.findByEmail("aluno@ufpb.br")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha_errada", "encoded_pass")).thenReturn(false);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("E-mail ou senha incorretos."));
        }

        @Test
        @DisplayName("POST /api/auth/refresh - Cookie Ausente retorna 401 Unauthorized")
        void shouldReturnUnauthorizedWhenRefreshCookieMissing() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Refresh Token ausente. Faça login novamente."));
        }

        @Test
        @DisplayName("POST /api/auth/refresh - Token Inválido ou Expirado retorna 401 Unauthorized")
        void shouldReturnUnauthorizedWhenRefreshTokenInvalid() throws Exception {
            String tokenStr = "invalid-or-expired-token";

            when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refresh_token", tokenStr)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Refresh Token inválido ou revogado."));
        }
    }
}
