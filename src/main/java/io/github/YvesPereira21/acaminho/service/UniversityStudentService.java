package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.model.UniversityStudent;
import io.github.YvesPereira21.acaminho.repository.UniversityStudentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UniversityStudentService {

    private final UniversityStudentRepository universityStudentRepository;

    public UniversityStudentService(UniversityStudentRepository universityStudentRepository) {
        this.universityStudentRepository = universityStudentRepository;
    }

    public UniversityStudent createUniversityStudentAccount(UniversityStudent universityStudent) {
        return universityStudentRepository.save(universityStudent);
    }

    public UniversityStudent getUniversityStudentById(UUID universityStudentId) {
        return universityStudentRepository.findById(universityStudentId)
                .orElseThrow();
    }

    public void deleteUniversityStudentById(UUID universityStudentId) {
        universityStudentRepository.deleteById(universityStudentId);
    }
}