package com.pawconnect.dog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DogRepository extends JpaRepository<Dog, UUID> {

    List<Dog> findByPetParentIdAndIsActiveTrue(UUID petParentId);

    Optional<Dog> findByIdAndPetParentId(UUID id, UUID petParentId);

    // Vets with active consent grant can access dog
    @Query("SELECT d FROM Dog d WHERE d.id = :dogId AND EXISTS (" +
            "SELECT cg FROM ConsentGrant cg WHERE cg.dogId = :dogId AND cg.vetId = :vetId " +
            "AND cg.revokedAt IS NULL AND (cg.expiresAt IS NULL OR cg.expiresAt > CURRENT_TIMESTAMP))")
    Optional<Dog> findByIdAndConsentedVet(@Param("dogId") UUID dogId, @Param("vetId") UUID vetId);
}
