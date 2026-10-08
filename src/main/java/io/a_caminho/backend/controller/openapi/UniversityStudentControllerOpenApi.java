package io.a_caminho.backend.controller.openapi;

import io.a_caminho.backend.config.OpenApiConfig;
import io.a_caminho.backend.dto.student.StudentCreateDTO;
import io.a_caminho.backend.dto.student.StudentDTO;
import io.a_caminho.backend.dto.student.StudentUpdateDTO;
import io.a_caminho.backend.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Estudantes Universitários", description = "Endpoints de gerenciamento e auto-cadastro de estudantes universitários")
public interface UniversityStudentControllerOpenApi {

    @Operation(
            summary = "Cadastra um novo estudante universitário",
            description = "Cria uma conta de usuário vinculada com a role ESTUDANTE e registra as informações acadêmicas do estudante associando-o ao município de origem e à universidade de destino."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Estudante cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos fornecidos no corpo da requisição",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Município ou Universidade informados não foram encontrados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito: e-mail, CPF ou matrícula já cadastrados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<StudentDTO> registerStudent(StudentCreateDTO request);

    @Operation(
            summary = "Busca um estudante universitário pelo ID",
            description = "Recupera os dados de perfil e acadêmicos do estudante universitário correspondente ao ID informado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados do estudante recuperados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estudante não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<StudentDTO> getStudent(
            @Parameter(description = "Identificador único do estudante universitário", required = true) UUID studentId
    );

    @Operation(
            summary = "Atualiza os dados cadastrais do estudante universitário",
            description = "Permite a atualização dos campos informados no DTO. Apenas o próprio estudante titular da conta possui permissão para atualizá-la."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Dados do estudante atualizados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos fornecidos no corpo da requisição",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o usuário autenticado não é o proprietário desta conta",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estudante ou Universidade informados não encontrados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<Void> updateStudent(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "Identificador único do estudante universitário", required = true) UUID studentId,
            StudentUpdateDTO updateDTO
    );

    @Operation(
            summary = "Exclui a conta de um estudante universitário",
            description = "Remove a conta do estudante universitário informado. Apenas o próprio estudante proprietário ou um Administrador do sistema possuem permissão para realizar esta exclusão."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Conta do estudante removida com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado: o usuário autenticado não tem permissão para excluir esta conta",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estudante ou usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
    ResponseEntity<Void> deleteStudentAccount(
            @Parameter(hidden = true) UUID userId,
            @Parameter(description = "Identificador único do estudante universitário", required = true) UUID studentId
    );
}
