package com.pawconnect.ngo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NGOWelfareCampaignRepository extends JpaRepository<NGOWelfareCampaign, UUID> {
    List<NGOWelfareCampaign> findByStatusOrderByCreatedAtDesc(String status);
    List<NGOWelfareCampaign> findByNgoIdOrderByCreatedAtDesc(UUID ngoId);
}
