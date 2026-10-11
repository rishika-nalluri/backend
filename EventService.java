package com.pawconnect.event;

import com.pawconnect.profile.NGOProfileRepository;
import com.pawconnect.reward.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRsvpRepository eventRsvpRepository;
    private final NGOProfileRepository ngoProfileRepository;
    private final CoinService coinService;

    @Transactional(readOnly = true)
    public List<Event> getUpcomingEvents(String city) {
        if (city != null && !city.isBlank()) {
            return eventRepository.findByCityIgnoreCaseAndEventDateAfterOrderByEventDateAsc(city, Instant.now());
        }
        return eventRepository.findByEventDateAfterOrderByEventDateAsc(Instant.now());
    }

    @Transactional
    public Event createEvent(UUID ngoId, Event event) {
        boolean isVerifiedNGO = ngoProfileRepository.findById(ngoId)
                .map(ngo -> "VERIFIED".equalsIgnoreCase(ngo.getVerificationStatus()))
                .orElse(false);

        if (!isVerifiedNGO) {
            throw new AccessDeniedException("Only verified NGOs can post community events and drives.");
        }

        event.setId(null);
        event.setPostedByNgoId(ngoId);
        return eventRepository.save(event);
    }

    @Transactional
    public EventRsvp rsvpToEvent(UUID eventId, UUID profileId, String status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        EventRsvp rsvp = eventRsvpRepository.findByEventIdAndProfileId(eventId, profileId)
                .orElse(EventRsvp.builder().eventId(eventId).profileId(profileId).build());

        rsvp.setRsvpStatus(status.toUpperCase());
        EventRsvp saved = eventRsvpRepository.save(rsvp);

        // Award reward coins for attending/joining welfare events
        try {
            coinService.awardCoins(profileId, "EVENT_ATTENDANCE", eventId);
        } catch (Exception e) {
            log.warn("Could not award coins for event RSVP: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<EventRsvp> getMyRsvps(UUID profileId) {
        return eventRsvpRepository.findByProfileId(profileId);
    }
}
