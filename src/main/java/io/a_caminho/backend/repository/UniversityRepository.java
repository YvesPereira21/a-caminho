package io.a_caminho.backend.repository;

import io.a_caminho.backend.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UniversityRepository extends JpaRepository<University, UUID> {
    List<University> findAllByCampusContainingIgnoreCaseAndCity_State_StateName(String name, String stateName);
}
