package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.student.StudentCreateDTO;
import io.a_caminho.backend.dto.student.StudentDTO;
import io.a_caminho.backend.dto.student.StudentUpdateDTO;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.security.UserPrincipal;
import io.a_caminho.backend.service.UniversityStudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes Unitários - UniversityStudentController")
class UniversityStudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UniversityStudentService studentService;

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("POST /api/university-students - Cadastra estudante com sucesso (201 Created)")
        void shouldRegisterStudentSuccessfully() throws Exception {
            UUID municipalityId = UUID.randomUUID();
            UUID universityId = UUID.randomUUID();

            StudentCreateDTO request = new StudentCreateDTO(
                    "Carlos Alberto",
                    "carlos@ufpb.br",
                    "senhaSegura123",
                    "11122233344",
                    "2023001234",
                    "Engenharia",
                    4,
                    LocalDate.now(),
                    municipalityId,
                    universityId
            );

            StudentDTO response = new StudentDTO(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Carlos Alberto",
                    "carlos@ufpb.br",
                    "11122233344",
                    "2023001234",
                    "Engenharia",
                    4,
                    LocalDate.now(),
                    municipalityId,
                    universityId
            );

            when(studentService.registerStudent(any(StudentCreateDTO.class))).thenReturn(response);

            mockMvc.perform(post("/api/university-students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.studentName").value("Carlos Alberto"))
                    .andExpect(jsonPath("$.email").value("carlos@ufpb.br"));
        }

        @Test
        @DisplayName("POST /api/students - Cadastra estudante com sucesso na rota alternativa (201 Created)")
        void shouldRegisterStudentSuccessfullyOnAlternativeRoute() throws Exception {
            UUID municipalityId = UUID.randomUUID();
            UUID universityId = UUID.randomUUID();

            StudentCreateDTO request = new StudentCreateDTO(
                    "Carlos Alberto",
                    "carlos@ufpb.br",
                    "senhaSegura123",
                    "11122233344",
                    "2023001234",
                    "Engenharia",
                    4,
                    LocalDate.now(),
                    municipalityId,
                    universityId
            );

            StudentDTO response = new StudentDTO(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Carlos Alberto",
                    "carlos@ufpb.br",
                    "11122233344",
                    "2023001234",
                    "Engenharia",
                    4,
                    LocalDate.now(),
                    municipalityId,
                    universityId
            );

            when(studentService.registerStudent(any(StudentCreateDTO.class))).thenReturn(response);

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.studentName").value("Carlos Alberto"));
        }

        @Test
        @DisplayName("GET /api/university-students/{studentId} - Retorna dados do estudante quando autenticado (200 OK)")
        void shouldGetStudentSuccessfullyWhenAuthenticated() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            StudentDTO response = new StudentDTO(
                    studentId,
                    userId,
                    "Carlos Alberto",
                    "carlos@ufpb.br",
                    "11122233344",
                    "2023001234",
                    "Engenharia",
                    4,
                    LocalDate.now(),
                    UUID.randomUUID(),
                    UUID.randomUUID()
            );

            when(studentService.getStudent(studentId)).thenReturn(response);

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(userId).email("carlos@ufpb.br").role(UserRole.STUDENT).build());

            mockMvc.perform(get("/api/university-students/{studentId}", studentId)
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.studentId").value(studentId.toString()))
                    .andExpect(jsonPath("$.studentName").value("Carlos Alberto"))
                    .andExpect(jsonPath("$.email").value("carlos@ufpb.br"));
        }

        @Test
        @DisplayName("PUT /api/university-students/{studentId} - Atualiza dados com sucesso com @AuthenticationPrincipal(expression = 'id') (204 No Content)")
        void shouldUpdateStudentSuccessfullyWhenOwnerAuthenticated() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID authUserId = UUID.randomUUID();

            StudentUpdateDTO updateDTO = new StudentUpdateDTO(
                    "Carlos Silva Atualizado",
                    "11122233344",
                    "2023009999",
                    "Ciência da Computação",
                    5,
                    null
            );

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(authUserId).email("carlos@ufpb.br").role(UserRole.STUDENT).build());

            mockMvc.perform(put("/api/university-students/{studentId}", studentId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNoContent());

            verify(studentService).updateStudent(authUserId, studentId, updateDTO);
        }

        @Test
        @DisplayName("DELETE /api/university-students/{studentId} - Exclui conta com sucesso com @AuthenticationPrincipal(expression = 'id') (204 No Content)")
        void shouldDeleteStudentSuccessfullyWhenAuthenticated() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID authUserId = UUID.randomUUID();

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(authUserId).email("carlos@ufpb.br").role(UserRole.STUDENT).build());

            mockMvc.perform(delete("/api/university-students/{studentId}", studentId)
                            .with(user(principal)))
                    .andExpect(status().isNoContent());

            verify(studentService).deleteStudentAccount(authUserId, studentId);
        }

        @Test
        @DisplayName("DELETE /api/university-students/{studentId} - Exclui conta com sucesso com perfil ADMIN via @PreAuthorize (204 No Content)")
        void shouldDeleteStudentSuccessfullyWhenAdminAuthenticated() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID adminUserId = UUID.randomUUID();

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(adminUserId).email("admin@acaminho.io").role(UserRole.ADMIN).build());

            mockMvc.perform(delete("/api/university-students/{studentId}", studentId)
                            .with(user(principal)))
                    .andExpect(status().isNoContent());

            verify(studentService).deleteStudentAccount(adminUserId, studentId);
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("POST /api/university-students - Falha com 400 Bad Request quando dados obrigatórios são inválidos")
        void shouldReturnBadRequestWhenStudentCreateDTOIsInvalid() throws Exception {
            StudentCreateDTO invalidRequest = new StudentCreateDTO(
                    "",
                    "invalid-email-format",
                    "123",
                    "",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            mockMvc.perform(post("/api/university-students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("POST /api/university-students - Falha com 409 Conflict se e-mail/CPF/matrícula já cadastrados")
        void shouldFailWhenEmailAlreadyRegistered() throws Exception {
            StudentCreateDTO request = new StudentCreateDTO(
                    "Carlos Alberto",
                    "existente@ufpb.br",
                    "senhaSegura123",
                    "11122233344",
                    null,
                    null,
                    null,
                    null,
                    UUID.randomUUID(),
                    UUID.randomUUID()
            );

            when(studentService.registerStudent(any(StudentCreateDTO.class)))
                    .thenThrow(new io.a_caminho.backend.exception.ObjectAlreadyExistsException("Usuário com essas informações já foi cadastrado"));

            mockMvc.perform(post("/api/university-students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Usuário com essas informações já foi cadastrado"));
        }

        @Test
        @DisplayName("GET /api/university-students/{studentId} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenGetStudentUnauthenticated() throws Exception {
            UUID studentId = UUID.randomUUID();

            mockMvc.perform(get("/api/university-students/{studentId}", studentId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/university-students/{studentId} - Falha com 404 Not Found quando estudante não existe")
        void shouldReturnNotFoundWhenStudentDoesNotExist() throws Exception {
            UUID studentId = UUID.randomUUID();
            when(studentService.getStudent(studentId))
                    .thenThrow(new io.a_caminho.backend.exception.ObjectNotFoundException("Essa conta não existe"));

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(UUID.randomUUID()).email("carlos@ufpb.br").role(UserRole.STUDENT).build());

            mockMvc.perform(get("/api/university-students/{studentId}", studentId)
                            .with(user(principal)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Essa conta não existe"));
        }

        @Test
        @DisplayName("PUT /api/university-students/{studentId} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenUpdateStudentUnauthenticated() throws Exception {
            UUID studentId = UUID.randomUUID();
            StudentUpdateDTO updateDTO = new StudentUpdateDTO("Novo Nome", null, null, null, null, null);

            mockMvc.perform(put("/api/university-students/{studentId}", studentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT /api/university-students/{studentId} - Falha com 403 Forbidden via @PreAuthorize quando usuário tem role ADMIN")
        void shouldReturnForbiddenWhenAdminAttemptsToUpdateStudent() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID adminUserId = UUID.randomUUID();
            StudentUpdateDTO updateDTO = new StudentUpdateDTO("Novo Nome", null, null, null, null, null);

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(adminUserId).email("admin@acaminho.io").role(UserRole.ADMIN).build());

            mockMvc.perform(put("/api/university-students/{studentId}", studentId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT /api/university-students/{studentId} - Falha com 403 Forbidden via @PreAuthorize quando usuário tem role MUNICIPALITY")
        void shouldReturnForbiddenWhenMunicipalityAttemptsToUpdateStudent() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID municipalityUserId = UUID.randomUUID();
            StudentUpdateDTO updateDTO = new StudentUpdateDTO("Novo Nome", null, null, null, null, null);

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(municipalityUserId).email("prefeitura@municipio.gov.br").role(UserRole.MUNICIPALITY).build());

            mockMvc.perform(put("/api/university-students/{studentId}", studentId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT /api/university-students/{studentId} - Falha com 403 Forbidden quando usuário STUDENT não for o proprietário")
        void shouldReturnForbiddenWhenUserNotOwnerOnUpdate() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID authUserId = UUID.randomUUID();

            StudentUpdateDTO updateDTO = new StudentUpdateDTO(
                    "Novo Nome", null, null, null, null, null
            );

            doThrow(new io.a_caminho.backend.exception.UserIsNotOwnerException("Você não possui permissão para isso."))
                    .when(studentService).updateStudent(authUserId, studentId, updateDTO);

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(authUserId).email("outro@ufpb.br").role(UserRole.STUDENT).build());

            mockMvc.perform(put("/api/university-students/{studentId}", studentId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Você não possui permissão para isso."));
        }

        @Test
        @DisplayName("PUT /api/university-students/{studentId} - Falha com 404 Not Found quando estudante não for encontrado")
        void shouldReturnNotFoundWhenStudentNotFoundOnUpdate() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID authUserId = UUID.randomUUID();

            StudentUpdateDTO updateDTO = new StudentUpdateDTO(
                    "Novo Nome", null, null, null, null, null
            );

            doThrow(new io.a_caminho.backend.exception.ObjectNotFoundException("Essa conta não existe."))
                    .when(studentService).updateStudent(authUserId, studentId, updateDTO);

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(authUserId).email("carlos@ufpb.br").role(UserRole.STUDENT).build());

            mockMvc.perform(put("/api/university-students/{studentId}", studentId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Essa conta não existe."));
        }

        @Test
        @DisplayName("DELETE /api/university-students/{studentId} - Falha com 401 Unauthorized quando não autenticado")
        void shouldReturnUnauthorizedWhenDeleteStudentUnauthenticated() throws Exception {
            UUID studentId = UUID.randomUUID();

            mockMvc.perform(delete("/api/university-students/{studentId}", studentId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/university-students/{studentId} - Falha com 403 Forbidden via @PreAuthorize quando usuário tem role MUNICIPALITY")
        void shouldReturnForbiddenWhenMunicipalityAttemptsToDeleteStudent() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID municipalityUserId = UUID.randomUUID();

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(municipalityUserId).email("prefeitura@municipio.gov.br").role(UserRole.MUNICIPALITY).build());

            mockMvc.perform(delete("/api/university-students/{studentId}", studentId)
                            .with(user(principal)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/university-students/{studentId} - Falha com 403 Forbidden via @PreAuthorize quando usuário tem role BUS_DRIVER")
        void shouldReturnForbiddenWhenBusDriverAttemptsToDeleteStudent() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID driverUserId = UUID.randomUUID();

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(driverUserId).email("motorista@municipio.gov.br").role(UserRole.BUS_DRIVER).build());

            mockMvc.perform(delete("/api/university-students/{studentId}", studentId)
                            .with(user(principal)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/university-students/{studentId} - Falha com 403 Forbidden quando usuário STUDENT não tiver permissão de dono")
        void shouldReturnForbiddenWhenNoPermissionOnDelete() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID authUserId = UUID.randomUUID();

            doThrow(new io.a_caminho.backend.exception.UserDoesNotHavePermissionException("Você não possui permissão para isso."))
                    .when(studentService).deleteStudentAccount(authUserId, studentId);

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(authUserId).email("outro@ufpb.br").role(UserRole.STUDENT).build());

            mockMvc.perform(delete("/api/university-students/{studentId}", studentId)
                            .with(user(principal)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Você não possui permissão para isso."));
        }

        @Test
        @DisplayName("DELETE /api/university-students/{studentId} - Falha com 404 Not Found quando estudante não existir")
        void shouldReturnNotFoundWhenStudentNotFoundOnDelete() throws Exception {
            UUID studentId = UUID.randomUUID();
            UUID authUserId = UUID.randomUUID();

            doThrow(new io.a_caminho.backend.exception.ObjectNotFoundException("Essa conta não existe"))
                    .when(studentService).deleteStudentAccount(authUserId, studentId);

            UserPrincipal principal = UserPrincipal.create(User.builder().userId(authUserId).email("carlos@ufpb.br").role(UserRole.STUDENT).build());

            mockMvc.perform(delete("/api/university-students/{studentId}", studentId)
                            .with(user(principal)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Essa conta não existe"));
        }
    }
}
