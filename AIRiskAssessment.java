package com.pawconnect.health;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_risk_assessments", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "dog_id", nullable = false)
    private UUID dogId;

    @Column(name = "source_symptom_log_id")
    private UUID sourceSymptomLogId;

    @Column(name = "risk_level", nullable = false, length = 32)
    private String riskLevel; // LOW, MODERATE, CONSULT_VET

    @Column(name = "confidence_score", precision = 4, scale = 3)
    private BigDecimal confidenceScore;

    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Builder.Default
    @Column(name = "model_provider", nullable = false, length = 50)
    private String modelProvider = "groq";

    @Column(name = "model_version", nullable = false, length = 100)
    private String modelVersion;

    @Column(name = "input_hash", length = 64)
    private String inputHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
