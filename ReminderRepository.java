package com.pawconnect.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {
    List<Reminder> findByPetParentIdOrderByDueDateAsc(UUID petParentId);
    List<Reminder> findByDogIdOrderByDueDateAsc(UUID dogId);
    List<Reminder> findByIsSentFalseAndDueDateBefore(Instant now);
}
