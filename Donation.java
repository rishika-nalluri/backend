package com.pawconnect.ngo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "donations", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "pet_parent_id", nullable = false)
    private UUID petParentId;

    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "donation_type", nullable = false, length = 32)
    private String donationType; // COIN, CASH

    @Builder.Default
    @Column(name = "coins_used")
    private Integer coinsUsed = 0;

    @Builder.Default
    @Column(name = "cash_amount", precision = 10, scale = 2)
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 32)
    private String status = "COMPLETED"; // PENDING, COMPLETED, FAILED

    @Column(name = "donor_notes", columnDefinition = "TEXT")
    private String donorNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
