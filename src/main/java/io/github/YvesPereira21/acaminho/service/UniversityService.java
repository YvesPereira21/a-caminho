package io.github.YvesPereira21.acaminho.service;

import io.github.YvesPereira21.acaminho.model.University;
import io.github.YvesPereira21.acaminho.repository.UniversityRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class UniversityService {

    private final UniversityRepository universityRepository;

    public UniversityService(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    public University createUniversity(University university) {
        return universityRepository.save(university);
    }

    public List<University> allUniversityContainingName(String universityName) {
        return universityRepository.findAllUniversityByNameContainingIgnoreCase(universityName);
    }

    public void deleteUniversity(UUID universityId) {
        universityRepository.deleteById(universityId);
    }
}