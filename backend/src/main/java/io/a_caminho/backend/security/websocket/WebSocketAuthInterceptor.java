package io.a_caminho.backend.security.websocket;

import io.a_caminho.backend.security.jwt.JwtTokenProvider;
import io.a_caminho.backend.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ExecutorChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            accessor = StompHeaderAccessor.wrap(message);
        }

        // 1. Intercepta a conexão inicial (CONNECT) para validar o JWT e autenticar a sessão
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = tokenProvider.validateToken(token);

                if (email != null) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    if (!accessor.isMutable()) {
                        accessor = StompHeaderAccessor.wrap(message);
                    }
                    accessor.setUser(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("WebSocket autenticado com sucesso para o usuário: {}", email);
                    return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
                } else {
                    log.warn("Tentativa de conexão WebSocket com token JWT inválido ou expirado");
                    throw new AccessDeniedException("Token JWT inválido ou expirado para WebSocket");
                }
            } else {
                log.warn("Tentativa de conexão WebSocket sem o cabeçalho Authorization");
                throw new AccessDeniedException("Cabeçalho Authorization com Bearer token é obrigatório no handshake STOMP");
            }
        }

        // 2. Propaga a autenticação da mensagem STOMP para o SecurityContext da thread de processamento
        if (accessor.getUser() instanceof Authentication auth) {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        SecurityContextHolder.clearContext();
    }

    /**
     * Executado na thread do handler (boundChannel), onde o @PreAuthorize é avaliado.
     * O preSend roda na thread do WebSocket, então o SecurityContext definido lá não chega aqui.
     */
    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && accessor.getUser() instanceof Authentication auth) {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        return message;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
        SecurityContextHolder.clearContext();
    }
}

