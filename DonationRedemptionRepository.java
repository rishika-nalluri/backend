package com.pawconnect.ngo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonationRedemptionRepository extends JpaRepository<DonationRedemption, UUID> {
    List<DonationRedemption> findByDonationId(UUID donationId);
}
