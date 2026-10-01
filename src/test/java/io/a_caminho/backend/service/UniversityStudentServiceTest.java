package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.student.StudentRegistrationRequestDTO;
import io.a_caminho.backend.dto.student.StudentResponseDTO;
import io.a_caminho.backend.mapper.UniversityStudentMapper;
import io.a_caminho.backend.model.Municipality;
import io.a_caminho.backend.model.University;
import io.a_caminho.backend.model.UniversityStudent;
import io.a_caminho.backend.model.User;
import io.a_caminho.backend.model.enums.UserRole;
import io.a_caminho.backend.repository.MunicipalityRepository;
import io.a_caminho.backend.repository.UniversityRepository;
import io.a_caminho.backend.repository.UniversityStudentRepository;
import io.a_caminho.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UniversityStudentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UniversityStudentRepository studentRepository;

    @Mock
    private MunicipalityRepository municipalityRepository;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UniversityStudentMapper studentMapper;

    @InjectMocks
    private UniversityStudentService studentService;

    private StudentRegistrationRequestDTO request;
    private Municipality municipality;
    private University university;

    @BeforeEach
    void setUp() {
        UUID municipalityId = UUID.randomUUID();
        UUID universityId = UUID.randomUUID();

        municipality = Municipality.builder().municipalityId(municipalityId).municipalityName("João Pessoa").build();
        university = University.builder().universityId(universityId).name("UFPB").build();

        request = new StudentRegistrationRequestDTO(
                "Estudante Silva",
                "estudante@ufpb.br",
                "senha123",
                "12345678900",
                "202600123",
                "Engenharia",
                3,
                LocalDate.now(),
                municipalityId,
                universityId
        );
    }

    @Test
    @DisplayName("Deve registrar estudante universitário com role STUDENT e senha com hash")
    void shouldRegisterStudentSuccessfully() {
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(studentRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(studentRepository.existsByRegistrationNumber(request.registrationNumber())).thenReturn(false);
        when(municipalityRepository.findById(request.municipalityId())).thenReturn(Optional.of(municipality));
        when(universityRepository.findById(request.universityId())).thenReturn(Optional.of(university));
        when(passwordEncoder.encode(request.password())).thenReturn("hashed_password");

        User savedUser = User.builder()
                .userId(UUID.randomUUID())
                .email(request.email())
                .password("hashed_password")
                .role(UserRole.STUDENT)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UniversityStudent studentEntity = UniversityStudent.builder()
                .studentName(request.studentName())
                .cpf(request.cpf())
                .registrationNumber(request.registrationNumber())
                .courseName(request.courseName())
                .currentPeriod(request.currentPeriod())
                .admissionDate(request.admissionDate())
                .build();
        when(studentMapper.toEntity(request)).thenReturn(studentEntity);

        UniversityStudent savedStudent = UniversityStudent.builder()
                .universityStudentId(UUID.randomUUID())
                .studentName(request.studentName())
                .cpf(request.cpf())
                .registrationNumber(request.registrationNumber())
                .courseName(request.courseName())
                .currentPeriod(request.currentPeriod())
                .admissionDate(request.admissionDate())
                .municipality(municipality)
                .university(university)
                .user(savedUser)
                .build();
        when(studentRepository.save(any(UniversityStudent.class))).thenReturn(savedStudent);

        StudentResponseDTO expectedResponse = new StudentResponseDTO(
                savedStudent.getUniversityStudentId(),
                savedUser.getUserId(),
                request.studentName(),
                request.email(),
                request.cpf(),
                request.registrationNumber(),
                request.courseName(),
                request.currentPeriod(),
                request.admissionDate(),
                municipality.getMunicipalityId(),
                university.getUniversityId()
        );
        when(studentMapper.toDTO(savedStudent)).thenReturn(expectedResponse);

        StudentResponseDTO response = studentService.registerStudent(request);

        assertNotNull(response);
        assertEquals(request.studentName(), response.studentName());
        assertEquals(request.email(), response.email());
        assertEquals(savedUser.getUserId(), response.userId());
        verify(passwordEncoder).encode("senha123");
        verify(userRepository).save(argThat(u -> u.getRole() == UserRole.STUDENT && u.getPassword().equals("hashed_password")));
    }

    @Test
    @DisplayName("Deve lançar exceção se o e-mail já estiver cadastrado")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.registerStudent(request));
        assertEquals("E-mail já cadastrado", ex.getMessage());
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção se o CPF já estiver cadastrado")
    void shouldThrowExceptionWhenCpfAlreadyExists() {
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(studentRepository.existsByCpf(request.cpf())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.registerStudent(request));
        assertEquals("CPF já cadastrado", ex.getMessage());
        verify(studentRepository, never()).save(any());
    }
}
