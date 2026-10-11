package com.pawconnect.profile;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vet_profiles", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetProfile {

    @Id
    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "profile_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Profile profile;

    @Column(name = "license_number", nullable = false)
    private String licenseNumber;

    @Column(name = "council_registration_no", nullable = false)
    private String councilRegistrationNo;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "clinic_name")
    private String clinicName;

    @Column(name = "clinic_address", columnDefinition = "TEXT")
    private String clinicAddress;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "verification_status", nullable = false, length = 32)
    private String verificationStatus; // PENDING, VERIFIED, REJECTED

    @Column(name = "verification_doc_url")
    private String verificationDocUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
