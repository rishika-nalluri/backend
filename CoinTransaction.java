package com.pawconnect.reward;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coin_transactions", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "pet_parent_id", nullable = false)
    private UUID petParentId;

    @Column(name = "type", nullable = false, length = 32)
    private String type; // EARN, REDEEM

    @Column(name = "source", nullable = false, length = 100)
    private String source; // VACCINATION_LOG, SYMPTOM_LOG, STORE_PURCHASE, NGO_DONATION, etc.

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "reference_id")
    private UUID referenceId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
