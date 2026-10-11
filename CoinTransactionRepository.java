package com.pawconnect.reward;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, UUID> {

    List<CoinTransaction> findByPetParentIdOrderByCreatedAtDesc(UUID petParentId);

    @Query("SELECT ct FROM CoinTransaction ct WHERE ct.petParentId = :petParentId ORDER BY ct.createdAt DESC LIMIT 1")
    Optional<CoinTransaction> findLatestByPetParentId(@Param("petParentId") UUID petParentId);
}
