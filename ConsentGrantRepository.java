package com.pawconnect.health;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentGrantRepository extends JpaRepository<ConsentGrant, UUID> {

    List<ConsentGrant> findByDogId(UUID dogId);

    List<ConsentGrant> findByPetParentId(UUID petParentId);

    List<ConsentGrant> findByVetIdAndRevokedAtIsNull(UUID vetId);

    @Query("SELECT cg FROM ConsentGrant cg WHERE cg.dogId = :dogId AND cg.vetId = :vetId " +
            "AND cg.revokedAt IS NULL AND (cg.expiresAt IS NULL OR cg.expiresAt > CURRENT_TIMESTAMP)")
    Optional<ConsentGrant> findActiveGrant(@Param("dogId") UUID dogId, @Param("vetId") UUID vetId);
}
