package com.pawconnect.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRsvpRepository extends JpaRepository<EventRsvp, UUID> {
    Optional<EventRsvp> findByEventIdAndProfileId(UUID eventId, UUID profileId);
    List<EventRsvp> findByProfileId(UUID profileId);
    List<EventRsvp> findByEventId(UUID eventId);
    long countByEventIdAndRsvpStatus(UUID eventId, String rsvpStatus);
}
