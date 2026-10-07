package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.student.StudentCreateDTO;
import io.a_caminho.backend.dto.student.StudentDTO;
import io.a_caminho.backend.dto.student.StudentUpdateDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerAndAdminException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - UniversityStudentService")
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

    private StudentCreateDTO request;
    private Municipality municipality;
    private University university;

    @BeforeEach
    void setUp() {
        UUID municipalityId = UUID.randomUUID();
        UUID universityId = UUID.randomUUID();

        municipality = Municipality.builder().municipalityId(municipalityId).municipalityName("João Pessoa").build();
        university = University.builder().universityId(universityId).name("UFPB").build();

        request = new StudentCreateDTO(
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

    @Nested
    @DisplayName("Cenários de registerStudent (Cadastro de Estudante)")
    class RegisterStudentTests {

        @Test
        @DisplayName("Deve registrar estudante universitário com sucesso gerando hash de senha e associando perfil")
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

            StudentDTO expectedResponse = new StudentDTO(
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

            StudentDTO response = studentService.registerStudent(request);

            assertNotNull(response);
            assertEquals(request.studentName(), response.studentName());
            assertEquals(request.email(), response.email());
            assertEquals(savedUser.getUserId(), response.userId());
            verify(passwordEncoder).encode("senha123");
            verify(userRepository).save(argThat(u -> u.getRole() == UserRole.STUDENT && u.getPassword().equals("hashed_password")));
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException se o e-mail já estiver cadastrado")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            ObjectAlreadyExistsException ex = assertThrows(
                    ObjectAlreadyExistsException.class,
                    () -> studentService.registerStudent(request));
            assertEquals("Usuário com essas informações já foi cadastrado", ex.getMessage());
            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectAlreadyExistsException se o CPF já estiver cadastrado")
        void shouldThrowExceptionWhenCpfAlreadyExists() {
            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(studentRepository.existsByCpf(request.cpf())).thenReturn(true);

            ObjectAlreadyExistsException ex = assertThrows(
                    ObjectAlreadyExistsException.class,
                    () -> studentService.registerStudent(request));
            assertEquals("Usuário com essas informações já foi cadastrado", ex.getMessage());
            verify(studentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de getStudent (Busca de Estudante por ID)")
    class GetStudentTests {

        @Test
        @DisplayName("Deve buscar estudante por ID com sucesso")
        void shouldGetStudentByIdSuccessfully() {
            UUID studentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            User user = User.builder().userId(userId).email("estudante@ufpb.br").role(UserRole.STUDENT).build();
            UniversityStudent student = UniversityStudent.builder()
                    .universityStudentId(studentId)
                    .studentName("Carlos Alberto")
                    .cpf("12345678900")
                    .user(user)
                    .municipality(municipality)
                    .university(university)
                    .build();

            StudentDTO expectedDTO = new StudentDTO(
                    studentId, userId, "Carlos Alberto", "estudante@ufpb.br",
                    "12345678900", "202600123", "Engenharia", 3,
                    LocalDate.now(), municipality.getMunicipalityId(), university.getUniversityId()
            );

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(studentMapper.toDTO(student)).thenReturn(expectedDTO);

            StudentDTO result = studentService.getStudent(studentId);

            assertNotNull(result);
            assertEquals(studentId, result.studentId());
            assertEquals("Carlos Alberto", result.studentName());
            verify(studentRepository).findById(studentId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao buscar estudante inexistente")
        void shouldThrowExceptionWhenStudentNotFoundOnGet() {
            UUID studentId = UUID.randomUUID();
            when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

            assertThrows(ObjectNotFoundException.class,
                    () -> studentService.getStudent(studentId));
        }
    }

    @Nested
    @DisplayName("Cenários de updateStudent (Atualização de Estudante)")
    class UpdateStudentTests {

        @Test
        @DisplayName("Deve atualizar dados do estudante quando o usuário autenticado for o proprietário")
        void shouldUpdateStudentSuccessfullyWhenUserIsOwner() {
            UUID studentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            User user = User.builder().userId(userId).email("estudante@ufpb.br").role(UserRole.STUDENT).build();
            UniversityStudent student = UniversityStudent.builder()
                    .universityStudentId(studentId)
                    .studentName("Carlos Alberto")
                    .cpf("12345678900")
                    .user(user)
                    .build();

            StudentUpdateDTO updateDTO = new StudentUpdateDTO(
                    "Carlos Silva Atualizado", "12345678900", "202600999", "Ciência da Computação", 5, null
            );

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(studentMapper.updateEntityFromDTO(updateDTO, student)).thenReturn(student);

            studentService.updateStudent(userId, studentId, updateDTO);

            verify(studentRepository).findById(studentId);
            verify(studentMapper).updateEntityFromDTO(updateDTO, student);
            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("Deve atualizar universidade do estudante quando universityId for informado na atualização")
        void shouldUpdateStudentUniversityWhenUniversityIdProvided() {
            UUID studentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID newUniversityId = UUID.randomUUID();
            University newUniversity = University.builder().universityId(newUniversityId).name("UFCG").build();

            User user = User.builder().userId(userId).email("estudante@ufpb.br").role(UserRole.STUDENT).build();
            UniversityStudent student = UniversityStudent.builder()
                    .universityStudentId(studentId)
                    .user(user)
                    .build();

            StudentUpdateDTO updateDTO = new StudentUpdateDTO(
                    null, null, null, null, null, newUniversityId
            );

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
            when(universityRepository.findById(newUniversityId)).thenReturn(Optional.of(newUniversity));
            when(studentMapper.updateEntityFromDTO(updateDTO, student)).thenReturn(student);

            studentService.updateStudent(userId, studentId, updateDTO);

            verify(universityRepository).findById(newUniversityId);
            assertEquals(newUniversity, student.getUniversity());
            verify(studentRepository).save(student);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao tentar atualizar estudante inexistente")
        void shouldThrowExceptionWhenStudentNotFoundOnUpdate() {
            UUID studentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            StudentUpdateDTO updateDTO = new StudentUpdateDTO("Nome", null, null, null, null, null);

            when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

            assertThrows(ObjectNotFoundException.class,
                    () -> studentService.updateStudent(userId, studentId, updateDTO));

            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar UserIsNotOwnerException ao tentar atualizar conta de outro estudante")
        void shouldThrowUserIsNotOwnerExceptionWhenUserNotOwnerOnUpdate() {
            UUID studentId = UUID.randomUUID();
            UUID ownerUserId = UUID.randomUUID();
            UUID differentUserId = UUID.randomUUID();

            User owner = User.builder().userId(ownerUserId).role(UserRole.STUDENT).build();
            UniversityStudent student = UniversityStudent.builder()
                    .universityStudentId(studentId)
                    .user(owner)
                    .build();

            StudentUpdateDTO updateDTO = new StudentUpdateDTO(
                    "Novo Nome", null, null, null, null, null
            );

            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

            assertThrows(UserIsNotOwnerException.class,
                    () -> studentService.updateStudent(differentUserId, studentId, updateDTO));

            verify(studentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de deleteStudentAccount (Exclusão de Conta de Estudante)")
    class DeleteStudentAccountTests {

        @Test
        @DisplayName("Deve excluir conta de estudante com sucesso quando usuário for o proprietário")
        void shouldDeleteStudentAccountSuccessfullyWhenUserIsOwner() {
            UUID studentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            User user = User.builder().userId(userId).role(UserRole.STUDENT).build();
            UniversityStudent student = UniversityStudent.builder()
                    .universityStudentId(studentId)
                    .user(user)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

            studentService.deleteStudentAccount(userId, studentId);

            verify(studentRepository).deleteById(studentId);
        }

        @Test
        @DisplayName("Deve excluir conta de estudante com sucesso quando usuário for ADMIN")
        void shouldDeleteStudentAccountSuccessfullyWhenUserIsAdmin() {
            UUID studentId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();

            User admin = User.builder().userId(adminId).role(UserRole.ADMIN).build();
            User owner = User.builder().userId(ownerId).role(UserRole.STUDENT).build();
            UniversityStudent student = UniversityStudent.builder()
                    .universityStudentId(studentId)
                    .user(owner)
                    .build();

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

            studentService.deleteStudentAccount(adminId, studentId);

            verify(studentRepository).deleteById(studentId);
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao tentar excluir com usuário inexistente")
        void shouldThrowExceptionWhenUserNotFoundOnDelete() {
            UUID studentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ObjectNotFoundException.class,
                    () -> studentService.deleteStudentAccount(userId, studentId));

            verify(studentRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar ObjectNotFoundException ao tentar excluir estudante inexistente")
        void shouldThrowExceptionWhenStudentNotFoundOnDelete() {
            UUID studentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            User user = User.builder().userId(userId).role(UserRole.STUDENT).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

            assertThrows(ObjectNotFoundException.class,
                    () -> studentService.deleteStudentAccount(userId, studentId));

            verify(studentRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar UserDoesNotHavePermissionException ao tentar excluir conta de terceiro sem ser ADMIN")
        void shouldThrowPermissionExceptionWhenUserIsNotOwnerNorAdminOnDelete() {
            UUID studentId = UUID.randomUUID();
            UUID unauthorizedUserId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();

            User unauthorizedUser = User.builder().userId(unauthorizedUserId).role(UserRole.STUDENT).build();
            User owner = User.builder().userId(ownerId).role(UserRole.STUDENT).build();
            UniversityStudent student = UniversityStudent.builder()
                    .universityStudentId(studentId)
                    .user(owner)
                    .build();

            when(userRepository.findById(unauthorizedUserId)).thenReturn(Optional.of(unauthorizedUser));
            when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

            assertThrows(UserIsNotOwnerAndAdminException.class,
                    () -> studentService.deleteStudentAccount(unauthorizedUserId, studentId));

            verify(studentRepository, never()).deleteById(any());
        }
    }
}
