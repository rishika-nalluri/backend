package com.pawconnect.health;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SymptomLogRepository extends JpaRepository<SymptomLog, UUID> {
    List<SymptomLog> findByDogIdOrderByLoggedAtDesc(UUID dogId);
}
