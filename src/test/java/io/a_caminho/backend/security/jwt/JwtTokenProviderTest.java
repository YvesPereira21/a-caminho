package io.a_caminho.backend.security.jwt;

import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 60000;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("Deve gerar e validar Access Token JWT com sucesso retornando subject")
        void shouldGenerateAndValidateToken() {
            User user = User.builder()
                    .userId(UUID.randomUUID())
                    .email("estudante@ufpb.br")
                    .role(UserRole.STUDENT)
                    .build();

            String token = jwtTokenProvider.generateToken(user);

            assertNotNull(token);
            String subject = jwtTokenProvider.validateToken(token);
            assertEquals("estudante@ufpb.br", subject);
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("Deve retornar null para token malformado ou adulterado")
        void shouldReturnNullForMalformedToken() {
            assertNull(jwtTokenProvider.validateToken("token.invalido.aqui"));
        }

        @Test
        @DisplayName("Deve retornar null para token expirado")
        void shouldReturnNullForExpiredToken() throws InterruptedException {
            JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secret, 1);
            User user = User.builder()
                    .userId(UUID.randomUUID())
                    .email("test@test.com")
                    .role(UserRole.STUDENT)
                    .build();

            String token = shortLivedProvider.generateToken(user);

            Thread.sleep(10);

            assertNull(shortLivedProvider.validateToken(token));
        }
    }
}
