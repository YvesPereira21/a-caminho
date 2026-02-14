package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.dto.request.UniversityStudentRequestDTO;
import io.github.YvesPereira21.acaminho.dto.response.UniversityStudentResponseDTO;
import io.github.YvesPereira21.acaminho.enums.UserRole;
import io.github.YvesPereira21.acaminho.exception.ObjectNotFoundException;
import io.github.YvesPereira21.acaminho.mapper.UniversityStudentMapper;
import io.github.YvesPereira21.acaminho.model.Municipality;
import io.github.YvesPereira21.acaminho.model.University;
import io.github.YvesPereira21.acaminho.model.UniversityStudent;
import io.github.YvesPereira21.acaminho.model.User;
import io.github.YvesPereira21.acaminho.repository.MunicipalityRepository;
import io.github.YvesPereira21.acaminho.repository.UniversityRepository;
import io.github.YvesPereira21.acaminho.repository.UniversityStudentRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UniversityStudentService {

    private final UniversityStudentRepository universityStudentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final UniversityRepository universityRepository;
    private final UniversityStudentMapper universityStudentMapper;
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UniversityStudentService(UniversityStudentRepository universityStudentRepository, MunicipalityRepository municipalityRepository, UniversityRepository universityRepository, UniversityStudentMapper universityStudentMapper, UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.universityStudentRepository = universityStudentRepository;
        this.municipalityRepository = municipalityRepository;
        this.universityRepository = universityRepository;
        this.universityStudentMapper = universityStudentMapper;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public UniversityStudentResponseDTO createUniversityStudentAccount(UniversityStudentRequestDTO universityStudent) {
        Municipality municipality = municipalityRepository
                .findByMunicipalityName(universityStudent.municipalityName())
                .orElseThrow(() -> new ObjectNotFoundException("Prefeitura não encontrada."));
        University university = universityRepository
                .findById(universityStudent.universityId())
                .orElseThrow(() -> new ObjectNotFoundException("Universidade não encontrada."));

        userService.verifyUserAlreadyExists(universityStudent.user().email());

        User newUser = new User();
        newUser.setEmail(universityStudent.user().email());
        newUser.setPassword(bCryptPasswordEncoder.encode(universityStudent.user().password()));
        newUser.setRole(UserRole.BUSDRIVER);

        UniversityStudent newUniversityStudent = new  UniversityStudent();
        newUniversityStudent.setStudentName(universityStudent.studentName());
        newUniversityStudent.setMunicipality(municipality);
        newUniversityStudent.setUniversity(university);
        newUniversityStudent.setUser(newUser);

        return universityStudentMapper
                .toResponse(universityStudentRepository.save(newUniversityStudent));
    }

    public UniversityStudentResponseDTO getUniversityStudentById(UUID universityStudentId) {
        UniversityStudent universityStudent = universityStudentRepository
                .findById(universityStudentId)
                .orElseThrow(() -> new ObjectNotFoundException("Estudante não encontrado."));
        return universityStudentMapper.toResponse(universityStudent);
    }

    public List<UniversityStudentResponseDTO> listStudentsFromMunicipality(UUID municipalityUserId) {
        return universityStudentRepository.findAllByMunicipality_User_UserId(municipalityUserId)
                .stream()
                .map(universityStudentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<UniversityStudentResponseDTO> listStudentsFromUniversity(UUID universityId) {
        return universityStudentRepository.findAllByUniversity_UniversityId(universityId)
                .stream()
                .map(universityStudentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteUniversityStudentById(UUID universityStudentId) {
        UniversityStudent universityStudent = universityStudentRepository
                .findById(universityStudentId)
                .orElseThrow(() -> new ObjectNotFoundException("Estudante não encontrado."));
        universityStudentRepository.delete(universityStudent);
    }
}