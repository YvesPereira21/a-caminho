package io.a_caminho.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.a_caminho.backend.dto.auth.UserLoginDTO;
import io.a_caminho.backend.model.RefreshToken;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.UserRepository;
import io.a_caminho.backend.security.jwt.JwtTokenProvider;
import io.a_caminho.backend.security.service.RefreshTokenService;
import io.a_caminho.backend.security.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RoleAuthorizationTest.TestProtectedController.class)
class RoleAuthorizationTest {

    @RestController
    @RequestMapping("/api/test")
    static class TestProtectedController {

        @GetMapping("/student")
        @PreAuthorize("hasRole('STUDENT')")
        public ResponseEntity<Map<String, String>> studentOnly(@AuthenticationPrincipal UserDetails userDetails) {
            return ResponseEntity.ok(Map.of(
                    "message", "Área restrita de estudante acessada com sucesso",
                    "user", userDetails != null ? userDetails.getUsername() : "anonymous"
            ));
        }

        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, String>> adminOnly(@AuthenticationPrincipal UserDetails userDetails) {
            return ResponseEntity.ok(Map.of(
                    "message", "Área restrita de administrador acessada com sucesso",
                    "user", userDetails != null ? userDetails.getUsername() : "anonymous"
            ));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private UUID studentUserId;
    private UUID adminUserId;
    private String studentToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        studentUserId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();

        User studentUser = User.builder()
                .userId(studentUserId)
                .email("student@ufpb.br")
                .role(UserRole.STUDENT)
                .build();

        User adminUser = User.builder()
                .userId(adminUserId)
                .email("admin@acaminho.io")
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));

        studentToken = jwtTokenProvider.generateToken(studentUser);
        adminToken = jwtTokenProvider.generateToken(adminUser);
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("Acesso Autorizado (200) para role STUDENT em rota de estudante via Cookie")
        void shouldAllowStudentAccess() throws Exception {
            mockMvc.perform(get("/api/test/student")
                            .cookie(new Cookie(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, studentToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Acesso Autorizado (200) para role STUDENT via cabeçalho Authorization: Bearer")
        void shouldAllowStudentAccessWithBearerHeader() throws Exception {
            mockMvc.perform(get("/api/test/student")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Área restrita de estudante acessada com sucesso"))
                    .andExpect(jsonPath("$.user").value("student@ufpb.br"));
        }

        @Test
        @DisplayName("Fluxo Completo: Estudante realiza Login, obtém Access Token e acessa rota protegida")
        void shouldCompleteFullLoginAndAccessFlowForStudent() throws Exception {
            User studentUser = User.builder()
                    .userId(UUID.randomUUID())
                    .email("aluno.completo@ufpb.br")
                    .password(passwordEncoder.encode("senhaForte123"))
                    .role(UserRole.STUDENT)
                    .build();

            when(userRepository.findByEmail("aluno.completo@ufpb.br")).thenReturn(Optional.of(studentUser));

            RefreshToken mockRefreshToken = RefreshToken.builder()
                    .tokenId(UUID.randomUUID())
                    .user(studentUser)
                    .token("mock-refresh-token")
                    .expiryDate(Instant.now().plusSeconds(604800))
                    .build();
            when(refreshTokenService.createRefreshToken(any())).thenReturn(mockRefreshToken);
            when(refreshTokenService.getRefreshTokenDurationSeconds()).thenReturn(604800L);

            // 1. Estudante realiza login
            UserLoginDTO loginDTO = new UserLoginDTO("aluno.completo@ufpb.br", "senhaForte123");
            String responseContent = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.user.email").value("aluno.completo@ufpb.br"))
                    .andExpect(jsonPath("$.user.role").value("Estudante"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // 2. Extrai o accessToken retornado
            String extractedToken = JsonPath.read(responseContent, "$.accessToken");

            // 3. Estudante acessa rota restrita de estudante com o token recebido
            mockMvc.perform(get("/api/test/student")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + extractedToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Área restrita de estudante acessada com sucesso"))
                    .andExpect(jsonPath("$.user").value("aluno.completo@ufpb.br"));
        }

        @Test
        @DisplayName("Acesso Autorizado (200) para role ADMIN em rota de admin")
        void shouldAllowAdminAccess() throws Exception {
            mockMvc.perform(get("/api/test/admin")
                            .cookie(new Cookie(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, adminToken)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("Acesso Negado (401) para rota protegida sem token")
        void shouldDenyAccessWhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/test/student"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Acesso Negado (403) para role STUDENT tentando acessar rota de ADMIN via Bearer")
        void shouldDenyStudentAccessToAdminEndpointWithBearerHeader() throws Exception {
            mockMvc.perform(get("/api/test/admin")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Acesso Negado (403) para role STUDENT tentando acessar rota de ADMIN via Cookie")
        void shouldDenyStudentAccessToAdminEndpoint() throws Exception {
            mockMvc.perform(get("/api/test/admin")
                            .cookie(new Cookie(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, studentToken)))
                    .andExpect(status().isForbidden());
        }
    }
}
