package com.pawconnect.health;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationRecordRepository extends JpaRepository<MedicationRecord, UUID> {
    List<MedicationRecord> findByDogIdOrderByStartDateDesc(UUID dogId);
    List<MedicationRecord> findByDogIdAndIsOngoingTrue(UUID dogId);
}
