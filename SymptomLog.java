package com.pawconnect.health;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "symptom_logs", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "dog_id", nullable = false)
    private UUID dogId;

    @Column(name = "symptoms_text", nullable = false, columnDefinition = "TEXT")
    private String symptomsText;

    @Column(name = "appetite", nullable = false, length = 50)
    private String appetite; // NORMAL, REDUCED, NONE

    @Column(name = "stool_consistency", nullable = false, length = 50)
    private String stoolConsistency; // NORMAL, SOFT, WATERY, BLOODY

    @Builder.Default
    @Column(name = "vomiting", nullable = false)
    private Boolean vomiting = false;

    @Builder.Default
    @Column(name = "lethargy", nullable = false)
    private Boolean lethargy = false;

    @Builder.Default
    @Column(name = "water_intake", length = 50)
    private String waterIntake = "NORMAL"; // NORMAL, INCREASED, DECREASED

    @CreationTimestamp
    @Column(name = "logged_at", nullable = false, updatable = false)
    private Instant loggedAt;
}
