package com.pawconnect.health;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AIRiskAssessmentRepository extends JpaRepository<AIRiskAssessment, UUID> {
    List<AIRiskAssessment> findByDogIdOrderByCreatedAtDesc(UUID dogId);
    Optional<AIRiskAssessment> findBySourceSymptomLogId(UUID symptomLogId);
    Optional<AIRiskAssessment> findFirstByInputHash(String inputHash);
}
