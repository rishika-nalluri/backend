package com.pawconnect.health;

import com.pawconnect.dog.Dog;
import com.pawconnect.dog.DogRepository;
import com.pawconnect.reward.CoinService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final DogRepository dogRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final MedicationRecordRepository medicationRecordRepository;
    private final SymptomLogRepository symptomLogRepository;
    private final AIRiskAssessmentRepository aiRiskAssessmentRepository;
    private final HealthDocumentRepository healthDocumentRepository;
    private final ConsentGrantRepository consentGrantRepository;
    private final AIRiskService aiRiskService;
    private final CoinService coinService;

    private void verifyAccess(UUID dogId, UUID userId, String role) {
        Dog dog = dogRepository.findById(dogId)
                .orElseThrow(() -> new IllegalArgumentException("Dog not found: " + dogId));

        if ("PET_PARENT".equalsIgnoreCase(role)) {
            if (!dog.getPetParentId().equals(userId)) {
                throw new AccessDeniedException("Access denied: You do not own this dog profile.");
            }
        } else if ("VET".equalsIgnoreCase(role)) {
            boolean hasConsent = consentGrantRepository.findActiveGrant(dogId, userId).isPresent();
            if (!hasConsent) {
                throw new AccessDeniedException("Access denied: An active consent grant from the pet parent is required.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Unauthorized to access this dog's records.");
        }
    }

    // Vaccinations
    @Transactional
    public VaccinationRecord addVaccination(UUID dogId, UUID userId, String role, VaccinationRecord record) {
        verifyAccess(dogId, userId, role);
        Dog dog = dogRepository.findById(dogId).orElseThrow();
        record.setId(null);
        record.setDogId(dogId);
        VaccinationRecord saved = vaccinationRecordRepository.save(record);

        // Award reward coins to the dog's pet parent
        try {
            coinService.awardCoins(dog.getPetParentId(), "VACCINATION_LOG", saved.getId());
        } catch (Exception e) {
            log.warn("Failed to award coins for vaccination log: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<VaccinationRecord> getVaccinations(UUID dogId, UUID userId, String role) {
        verifyAccess(dogId, userId, role);
        return vaccinationRecordRepository.findByDogIdOrderByDateGivenDesc(dogId);
    }

    // Medications
    @Transactional
    public MedicationRecord addMedication(UUID dogId, UUID userId, String role, MedicationRecord record) {
        verifyAccess(dogId, userId, role);
        record.setId(null);
        record.setDogId(dogId);
        return medicationRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<MedicationRecord> getMedications(UUID dogId, UUID userId, String role) {
        verifyAccess(dogId, userId, role);
        return medicationRecordRepository.findByDogIdOrderByStartDateDesc(dogId);
    }

    // Symptom Logging + AI Screening End-to-End
    @Transactional
    public SymptomLogAndAssessment logSymptoms(UUID dogId, UUID userId, String role, SymptomLog logEntry) {
        verifyAccess(dogId, userId, role);
        Dog dog = dogRepository.findById(dogId).orElseThrow();

        logEntry.setId(null);
        logEntry.setDogId(dogId);
        SymptomLog savedLog = symptomLogRepository.save(logEntry);

        // Award PawCoins to pet parent
        try {
            coinService.awardCoins(dog.getPetParentId(), "SYMPTOM_LOG", savedLog.getId());
        } catch (Exception e) {
            log.warn("Failed to award coins for symptom log: {}", e.getMessage());
        }

        // Trigger AI Screening
        AIRiskService.DogHealthRiskInput riskInput = new AIRiskService.DogHealthRiskInput();
        riskInput.setDogId(dogId);
        riskInput.setSymptomLogId(savedLog.getId());
        riskInput.setSymptomsText(savedLog.getSymptomsText());
        riskInput.setAppetite(savedLog.getAppetite());
        riskInput.setStoolConsistency(savedLog.getStoolConsistency());
        riskInput.setVomiting(savedLog.getVomiting());
        riskInput.setLethargy(savedLog.getLethargy());
        riskInput.setWaterIntake(savedLog.getWaterIntake());

        AIRiskAssessment assessment = aiRiskService.assessRisk(riskInput);

        return SymptomLogAndAssessment.builder()
                .symptomLog(savedLog)
                .riskAssessment(assessment)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SymptomLog> getSymptomLogs(UUID dogId, UUID userId, String role) {
        verifyAccess(dogId, userId, role);
        return symptomLogRepository.findByDogIdOrderByLoggedAtDesc(dogId);
    }

    @Transactional(readOnly = true)
    public List<AIRiskAssessment> getRiskAssessments(UUID dogId, UUID userId, String role) {
        verifyAccess(dogId, userId, role);
        return aiRiskAssessmentRepository.findByDogIdOrderByCreatedAtDesc(dogId);
    }

    // Health Documents
    @Transactional
    public HealthDocument addHealthDocument(UUID dogId, UUID userId, String role, HealthDocument doc) {
        verifyAccess(dogId, userId, role);
        doc.setId(null);
        doc.setDogId(dogId);
        doc.setUploadedBy(userId);
        return healthDocumentRepository.save(doc);
    }

    @Transactional(readOnly = true)
    public List<HealthDocument> getHealthDocuments(UUID dogId, UUID userId, String role) {
        verifyAccess(dogId, userId, role);
        return healthDocumentRepository.findByDogIdOrderByCreatedAtDesc(dogId);
    }

    // Complete Longitudinal Health Dossier
    @Transactional(readOnly = true)
    public DogHealthDossier getHealthDossier(UUID dogId, UUID userId, String role) {
        verifyAccess(dogId, userId, role);
        Dog dog = dogRepository.findById(dogId).orElseThrow();

        return DogHealthDossier.builder()
                .dog(dog)
                .vaccinations(vaccinationRecordRepository.findByDogIdOrderByDateGivenDesc(dogId))
                .medications(medicationRecordRepository.findByDogIdOrderByStartDateDesc(dogId))
                .recentSymptoms(symptomLogRepository.findByDogIdOrderByLoggedAtDesc(dogId))
                .latestRiskAssessments(aiRiskAssessmentRepository.findByDogIdOrderByCreatedAtDesc(dogId))
                .documents(healthDocumentRepository.findByDogIdOrderByCreatedAtDesc(dogId))
                .build();
    }

    @Data
    @Builder
    public static class SymptomLogAndAssessment {
        private SymptomLog symptomLog;
        private AIRiskAssessment riskAssessment;
    }

    @Data
    @Builder
    public static class DogHealthDossier {
        private Dog dog;
        private List<VaccinationRecord> vaccinations;
        private List<MedicationRecord> medications;
        private List<SymptomLog> recentSymptoms;
        private List<AIRiskAssessment> latestRiskAssessments;
        private List<HealthDocument> documents;
    }
}
