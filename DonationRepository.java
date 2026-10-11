package com.pawconnect.ngo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {
    List<Donation> findByPetParentIdOrderByCreatedAtDesc(UUID petParentId);
    List<Donation> findByCampaignIdOrderByCreatedAtDesc(UUID campaignId);
    List<Donation> findByNgoIdOrderByCreatedAtDesc(UUID ngoId);
}
