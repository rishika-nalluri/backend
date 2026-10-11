package com.pawconnect.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByEventDateAfterOrderByEventDateAsc(Instant date);
    List<Event> findByCityIgnoreCaseAndEventDateAfterOrderByEventDateAsc(String city, Instant date);
    List<Event> findByPostedByNgoIdOrderByEventDateDesc(UUID ngoId);
}
