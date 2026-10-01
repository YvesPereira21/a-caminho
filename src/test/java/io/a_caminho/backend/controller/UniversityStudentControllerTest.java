package io.a_caminho.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a_caminho.backend.dto.student.StudentRegistrationRequestDTO;
import io.a_caminho.backend.dto.student.StudentResponseDTO;
import io.a_caminho.backend.service.UniversityStudentService;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UniversityStudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private UniversityStudentService studentService;

    @Test
    @DisplayName("POST /api/university-students - Sucesso")
    void shouldRegisterStudentSuccessfully() throws Exception {
        UUID municipalityId = UUID.randomUUID();
        UUID universityId = UUID.randomUUID();

        StudentRegistrationRequestDTO request = new StudentRegistrationRequestDTO(
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

        StudentResponseDTO response = new StudentResponseDTO(
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

        when(studentService.registerStudent(any(StudentRegistrationRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/university-students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentName").value("Carlos Alberto"))
                .andExpect(jsonPath("$.email").value("carlos@ufpb.br"));
    }

    @Test
    @DisplayName("POST /api/students - Sucesso na rota alternativa")
    void shouldRegisterStudentSuccessfullyOnAlternativeRoute() throws Exception {
        UUID municipalityId = UUID.randomUUID();
        UUID universityId = UUID.randomUUID();

        StudentRegistrationRequestDTO request = new StudentRegistrationRequestDTO(
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

        StudentResponseDTO response = new StudentResponseDTO(
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

        when(studentService.registerStudent(any(StudentRegistrationRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentName").value("Carlos Alberto"));
    }

    @Test
    @DisplayName("POST /api/university-students - E-mail já cadastrado")
    void shouldFailWhenEmailAlreadyRegistered() throws Exception {
        StudentRegistrationRequestDTO request = new StudentRegistrationRequestDTO(
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

        when(studentService.registerStudent(any(StudentRegistrationRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("E-mail já cadastrado"));

        mockMvc.perform(post("/api/university-students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }
}
