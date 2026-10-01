package io.a_caminho.backend.controller;

import io.a_caminho.backend.dto.auth.UserDTO;
import io.a_caminho.backend.dto.auth.UserLoginDTO;
import io.a_caminho.backend.exception.ApiError;
import io.a_caminho.backend.model.RefreshToken;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.repository.RefreshTokenRepository;
import io.a_caminho.backend.repository.UserRepository;
import io.a_caminho.backend.security.jwt.JwtTokenProvider;
import io.a_caminho.backend.security.service.RefreshTokenService;
import io.a_caminho.backend.security.util.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticação", description = "Endpoints para login, renovação de tokens JWT e encerramento de sessão")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtils cookieUtils;
    private final io.a_caminho.backend.mapper.UserMapper userMapper;

    @Operation(
            summary = "Realiza login com e-mail e senha",
            description = "Valida as credenciais do usuário. Em caso de sucesso, retorna o Access Token JWT e dados do usuário no corpo JSON, e anexa o Refresh Token em um cookie HttpOnly seguro."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"accessToken\": \"eyJhbGciOi...\", \"user\": {\"id\": \"123e4567-e89b-12d3-a456-426614174000\", \"name\": \"Carlos Alberto\", \"email\": \"aluno@ufpb.br\", \"role\": \"Estudante\"}}"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas (e-mail ou senha incorretos)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"E-mail ou senha incorretos.\"}"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de requisição inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
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

    @Operation(
            summary = "Renova o Access Token JWT",
            description = "Lê o Refresh Token armazenado no cookie HttpOnly 'refresh_token', valida sua vigência no banco de dados, executa a rotação (Refresh Token Rotation) e emite um novo Access Token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token de acesso renovado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"accessToken\": \"eyJhbGciOi...\"}"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh Token ausente, inválido ou expirado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Refresh Token ausente. Faça login novamente.\"}"))
            )
    })
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

        // Rotação: Apaga o Refresh Token antigo
        refreshTokenService.deleteByToken(refreshTokenStr);

        // Gera novo Refresh Token e novo Access Token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        String newAccessToken = tokenService.generateToken(user);
        ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(newRefreshToken.getToken(), refreshTokenService.getRefreshTokenDurationSeconds());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("accessToken", newAccessToken));
    }

    @Operation(
            summary = "Encerra a sessão do usuário (Logout)",
            description = "Revoga e exclui o Refresh Token ativo do banco de dados e limpa os cookies HttpOnly de autenticação no cliente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout efetuado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\": \"Logout realizado com sucesso.\"}"))
            )
    })
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
