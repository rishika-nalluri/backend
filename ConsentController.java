package com.pawconnect.health;

import com.pawconnect.security.UserPrincipal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/consent")
@RequiredArgsConstructor
public class ConsentController {

    private final ConsentService consentService;

    @PostMapping("/grant")
    public ResponseEntity<ConsentGrant> grantConsent(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody GrantConsentRequest request
    ) {
        ConsentGrant grant = consentService.grantConsent(
                principal.getId(),
                request.getDogId(),
                request.getVetId(),
                request.getExpiresAt(),
                request.getNotes()
        );
        return ResponseEntity.ok(grant);
    }

    @PostMapping("/revoke/{grantId}")
    public ResponseEntity<Void> revokeConsent(
            @PathVariable UUID grantId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        consentService.revokeConsent(principal.getId(), grantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dog/{dogId}")
    public ResponseEntity<List<ConsentGrant>> getGrantsForDog(
            @PathVariable UUID dogId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(consentService.getGrantsForDog(principal.getId(), dogId));
    }

    @GetMapping("/my-vet-access")
    public ResponseEntity<List<ConsentGrant>> getMyVetAccess(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(consentService.getGrantsForVet(principal.getId()));
    }

    @Data
    public static class GrantConsentRequest {
        private UUID dogId;
        private UUID vetId;
        private Instant expiresAt;
        private String notes;
    }
}
