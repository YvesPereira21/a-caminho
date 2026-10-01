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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public StudentResponseDTO registerStudent(StudentRegistrationRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        if (studentRepository.existsByCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        if (request.registrationNumber() != null && !request.registrationNumber().isBlank()
                && studentRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new IllegalArgumentException("Matrícula já cadastrada");
        }

        Municipality municipality = municipalityRepository.findById(request.municipalityId())
                .orElseThrow(() -> new IllegalArgumentException("Município não encontrado com ID: " + request.municipalityId()));

        University university = universityRepository.findById(request.universityId())
                .orElseThrow(() -> new IllegalArgumentException("Universidade não encontrada com ID: " + request.universityId()));

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
}
