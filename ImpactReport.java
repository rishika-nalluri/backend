package com.pawconnect.ngo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "impact_reports", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "report_text", nullable = false, columnDefinition = "TEXT")
    private String reportText;

    @Builder.Default
    @Column(name = "dogs_fed_count")
    private Integer dogsFedCount = 0;

    @Builder.Default
    @Column(name = "dogs_vaccinated_count")
    private Integer dogsVaccinatedCount = 0;

    @Builder.Default
    @Column(name = "dogs_treated_count")
    private Integer dogsTreatedCount = 0;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
