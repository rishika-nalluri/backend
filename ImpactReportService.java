package com.pawconnect.ngo;

import com.pawconnect.profile.NGOProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImpactReportService {

    private final ImpactReportRepository impactReportRepository;
    private final NGOProfileRepository ngoProfileRepository;

    @Transactional(readOnly = true)
    public List<ImpactReport> getAllReports() {
        return impactReportRepository.findAllByOrderByReportDateDesc();
    }

    @Transactional(readOnly = true)
    public List<ImpactReport> getReportsForCampaign(UUID campaignId) {
        return impactReportRepository.findByCampaignIdOrderByReportDateDesc(campaignId);
    }

    @Transactional
    public ImpactReport postImpactReport(UUID ngoId, ImpactReport report) {
        boolean isVerified = ngoProfileRepository.findById(ngoId)
                .map(n -> "VERIFIED".equalsIgnoreCase(n.getVerificationStatus()))
                .orElse(false);

        if (!isVerified) {
            throw new AccessDeniedException("Only verified NGOs can post impact reports.");
        }

        report.setId(null);
        report.setNgoId(ngoId);
        if (report.getReportDate() == null) {
            report.setReportDate(LocalDate.now());
        }
        return impactReportRepository.save(report);
    }
}
