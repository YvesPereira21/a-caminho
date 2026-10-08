package io.a_caminho.backend.controller.openapi;

import io.a_caminho.backend.dto.auth.UserLoginDTO;
import io.a_caminho.backend.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Autenticação", description = "Endpoints para login, renovação de tokens JWT e encerramento de sessão")
public interface AuthControllerOpenApi {

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
    ResponseEntity<?> login(UserLoginDTO loginDTO);

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
    ResponseEntity<?> refreshToken(String refreshTokenStr);

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
    ResponseEntity<?> logout(String refreshTokenStr);
}
