package io.a_caminho.backend.service;

import io.a_caminho.backend.dto.student.StudentCreateDTO;
import io.a_caminho.backend.dto.student.StudentDTO;
import io.a_caminho.backend.dto.student.StudentUpdateDTO;
import io.a_caminho.backend.exception.ObjectAlreadyExistsException;
import io.a_caminho.backend.exception.ObjectNotFoundException;
import io.a_caminho.backend.exception.UserIsNotOwnerAndAdminException;
import io.a_caminho.backend.exception.UserIsNotOwnerException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniversityStudentService {

    private final UserRepository userRepository;
    private final UniversityStudentRepository studentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniversityStudentMapper studentMapper;

    @Transactional
    public StudentDTO registerStudent(StudentCreateDTO request) {
        boolean userExist = checkStudentExists(request.email(), request.cpf(), request.registrationNumber());
        if (userExist) {
            throw new ObjectAlreadyExistsException("Usuário com essas informações já foi cadastrado");
        }

        Municipality municipality = municipalityRepository.findById(request.municipalityId())
                .orElseThrow(() -> new ObjectNotFoundException("Município não encontrado com ID: " + request.municipalityId()));

        University university = universityRepository.findById(request.universityId())
                .orElseThrow(() -> new ObjectNotFoundException("Universidade não encontrada com ID: " + request.universityId()));

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.STUDENT)
                .build();
        User savedUser = userRepository.save(user);

        UniversityStudent student = studentMapper.toEntity(request);
        student.setUser(savedUser);
        student.setMunicipality(municipality);
        student.setUniversity(university);

        UniversityStudent savedStudent = studentRepository.save(student);

        return studentMapper.toDTO(savedStudent);
    }

    @Transactional(readOnly = true)
    public StudentDTO getStudent(UUID studentId) {
        UniversityStudent universityStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("Essa conta não existe"));

        return studentMapper.toDTO(universityStudent);
    }

    @Transactional
    public void updateStudent(UUID authenticatedUserId, UUID studentId, StudentUpdateDTO updateDTO) {
        UniversityStudent universityStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("Essa conta não existe."));

        if (!universityStudent.getUser().getId().equals(authenticatedUserId)) {
            throw new UserIsNotOwnerException("Você não possui permissão para isso.");
        }

        if (updateDTO.universityId() != null) {
            University university = universityRepository.findById(updateDTO.universityId())
                    .orElseThrow(() -> new ObjectNotFoundException("Universidade não encontrada com ID: " + updateDTO.universityId()));
            universityStudent.setUniversity(university);
        }

        UniversityStudent studentUpdated = studentMapper.updateEntityFromDTO(updateDTO, universityStudent);
        studentRepository.save(studentUpdated);
    }

    @Transactional
    public void deleteStudentAccount(UUID authenticatedUserId, UUID studentId) {
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ObjectNotFoundException("Usuário inexistente"));
        UniversityStudent universityStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("Essa conta não existe"));

        if (!universityStudent.getUser().getId().equals(authenticatedUserId) && !user.getRole().equals(UserRole.ADMIN)) {
            throw new UserIsNotOwnerAndAdminException("Você não possui permissão para isso.");
        }

        studentRepository.deleteById(studentId);
    }

    private boolean checkStudentExists(String email, String cpf, String registrationNumber) {
        if (userRepository.existsByEmail(email)) {
            return true;
        }
        if (studentRepository.existsByCpf(cpf)) {
            return true;
        }
        if (registrationNumber != null && !registrationNumber.isBlank()) {
            return studentRepository.existsByRegistrationNumber(registrationNumber);
        }
        return false;
    }
}
