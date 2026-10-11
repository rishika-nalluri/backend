package com.pawconnect.ngo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImpactReportRepository extends JpaRepository<ImpactReport, UUID> {
    List<ImpactReport> findByCampaignIdOrderByReportDateDesc(UUID campaignId);
    List<ImpactReport> findByNgoIdOrderByReportDateDesc(UUID ngoId);
    List<ImpactReport> findAllByOrderByReportDateDesc();
}
