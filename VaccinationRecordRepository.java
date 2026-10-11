package com.pawconnect.health;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, UUID> {
    List<VaccinationRecord> findByDogIdOrderByDateGivenDesc(UUID dogId);
    List<VaccinationRecord> findByNextDueDateBetween(LocalDate start, LocalDate end);
}
