package com.pawconnect.event;

import com.pawconnect.security.UserPrincipal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<Event>> getEvents(@RequestParam(required = false) String city) {
        return ResponseEntity.ok(eventService.getUpcomingEvents(city));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ResponseEntity<Event> createEvent(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Event event
    ) {
        return ResponseEntity.ok(eventService.createEvent(principal.getId(), event));
    }

    @PostMapping("/{id}/rsvp")
    public ResponseEntity<EventRsvp> rsvpToEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody RsvpRequest request
    ) {
        return ResponseEntity.ok(eventService.rsvpToEvent(
                id,
                principal.getId(),
                request.getStatus() != null ? request.getStatus() : "GOING"
        ));
    }

    @GetMapping("/my-rsvps")
    public ResponseEntity<List<EventRsvp>> getMyRsvps(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(eventService.getMyRsvps(principal.getId()));
    }

    @Data
    public static class RsvpRequest {
        private String status;
    }
}
