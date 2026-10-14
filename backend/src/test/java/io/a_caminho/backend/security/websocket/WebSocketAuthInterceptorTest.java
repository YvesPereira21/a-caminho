package io.a_caminho.backend.security.websocket;

import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.security.jwt.JwtTokenProvider;
import io.a_caminho.backend.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade - WebSocketAuthInterceptor")
class WebSocketAuthInterceptorTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private MessageChannel messageChannel;

    private WebSocketAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        interceptor = new WebSocketAuthInterceptor(tokenProvider, customUserDetailsService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserPrincipal createPrincipal(UserRole role) {
        return createPrincipal("user@test.com", role);
    }

    private UserPrincipal createPrincipal(String email, UserRole role) {
        return UserPrincipal.create(User.builder()
                .userId(UUID.randomUUID())
                .email(email)
                .role(role)
                .password("secret")
                .build());
    }

    @Nested
    @DisplayName("Testes do comando CONNECT (Autenticação do Handshake STOMP)")
    class ConnectTests {

        @Test
        @DisplayName("Deve autenticar com sucesso quando o token JWT for válido")
        void shouldAuthenticateWithValidJwtToken() {
            String token = "valid-jwt-token";
            String email = "motorista@acaminho.com";
            UserPrincipal principal = createPrincipal(email, UserRole.BUS_DRIVER);

            when(tokenProvider.validateToken(token)).thenReturn(email);
            when(customUserDetailsService.loadUserByUsername(email)).thenReturn(principal);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setNativeHeader("Authorization", "Bearer " + token);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            Message<?> result = interceptor.preSend(message, messageChannel);

            assertThat(result).isNotNull();
            StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
            assertThat(resultAccessor.getUser()).isNotNull();
            assertThat(resultAccessor.getUser().getName()).isEqualTo(email);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(email);
        }

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando o token JWT for inválido ou expirado")
        void shouldThrowExceptionWhenTokenInvalid() {
            String token = "invalid-token";
            when(tokenProvider.validateToken(token)).thenReturn(null);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setNativeHeader("Authorization", "Bearer " + token);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Token JWT inválido ou expirado");
        }

        @Test
        @DisplayName("Deve lançar AccessDeniedException quando não houver cabeçalho Authorization")
        void shouldThrowExceptionWhenAuthorizationHeaderMissing() {
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Cabeçalho Authorization com Bearer token é obrigatório");
        }
    }

    @Nested
    @DisplayName("Testes do comando SEND e ciclo de vida (Propagação e limpeza de SecurityContext)")
    class SendAndLifecycleTests {

        @Test
        @DisplayName("Deve propagar Authentication para SecurityContextHolder ao interceptar envio")
        void shouldPropagateAuthenticationToSecurityContext() {
            UserPrincipal driverPrincipal = createPrincipal(UserRole.BUS_DRIVER);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(driverPrincipal, null, driverPrincipal.getAuthorities());

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
            accessor.setDestination("/app/location");
            accessor.setUser(auth);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            Message<?> result = interceptor.preSend(message, messageChannel);

            assertThat(result).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(auth);
        }

        @Test
        @DisplayName("Deve limpar SecurityContextHolder no afterSendCompletion")
        void shouldClearSecurityContextOnAfterSendCompletion() {
            UserPrincipal driverPrincipal = createPrincipal(UserRole.BUS_DRIVER);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(driverPrincipal, null, driverPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            interceptor.afterSendCompletion(message, messageChannel, true, null);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Deve permitir envio mesmo sem usuário sem gerar exceção no interceptor")
        void shouldAllowSendWithoutUser() {
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
            accessor.setDestination("/app/location");
            Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            Message<?> result = interceptor.preSend(message, messageChannel);

            assertThat(result).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}

