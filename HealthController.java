package com.pawconnect.health;

import com.pawconnect.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthRecordService healthRecordService;

    // Dossier
    @GetMapping("/dogs/{dogId}/dossier")
    public ResponseEntity<HealthRecordService.DogHealthDossier> getDogHealthDossier(
            @PathVariable UUID dogId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(healthRecordService.getHealthDossier(dogId, principal.getId(), principal.getRole()));
    }

    // Vaccinations and medications are served by the care module (/dogs/{dogId}/vaccinations, /medications).

    // Symptom Logs & AI Screening
    @GetMapping("/dogs/{dogId}/symptoms")
    public ResponseEntity<List<SymptomLog>> getSymptomLogs(
            @PathVariable UUID dogId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(healthRecordService.getSymptomLogs(dogId, principal.getId(), principal.getRole()));
    }

    @PostMapping("/dogs/{dogId}/symptoms")
    public ResponseEntity<HealthRecordService.SymptomLogAndAssessment> logSymptoms(
            @PathVariable UUID dogId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody SymptomLog logEntry
    ) {
        return ResponseEntity.ok(healthRecordService.logSymptoms(dogId, principal.getId(), principal.getRole(), logEntry));
    }

    @GetMapping("/dogs/{dogId}/ai-screenings")
    public ResponseEntity<List<AIRiskAssessment>> getRiskAssessments(
            @PathVariable UUID dogId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(healthRecordService.getRiskAssessments(dogId, principal.getId(), principal.getRole()));
    }

    // Health Documents
    @GetMapping("/dogs/{dogId}/documents")
    public ResponseEntity<List<HealthDocument>> getDocuments(
            @PathVariable UUID dogId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(healthRecordService.getHealthDocuments(dogId, principal.getId(), principal.getRole()));
    }

    @PostMapping("/dogs/{dogId}/documents")
    public ResponseEntity<HealthDocument> addDocument(
            @PathVariable UUID dogId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody HealthDocument document
    ) {
        return ResponseEntity.ok(healthRecordService.addHealthDocument(dogId, principal.getId(), principal.getRole(), document));
    }
}
