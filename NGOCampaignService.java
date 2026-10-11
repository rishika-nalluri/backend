package com.pawconnect.ngo;

import com.pawconnect.profile.NGOProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NGOCampaignService {

    private final NGOWelfareCampaignRepository campaignRepository;
    private final NGOProfileRepository ngoProfileRepository;

    @Transactional(readOnly = true)
    public List<NGOWelfareCampaign> getActiveCampaigns() {
        return campaignRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
    }

    @Transactional(readOnly = true)
    public Optional<NGOWelfareCampaign> getCampaign(UUID id) {
        return campaignRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<NGOWelfareCampaign> getMyCampaigns(UUID ngoId) {
        return campaignRepository.findByNgoIdOrderByCreatedAtDesc(ngoId);
    }

    @Transactional
    public NGOWelfareCampaign createCampaign(UUID ngoId, NGOWelfareCampaign campaign) {
        boolean isVerified = ngoProfileRepository.findById(ngoId)
                .map(n -> "VERIFIED".equalsIgnoreCase(n.getVerificationStatus()))
                .orElse(false);

        if (!isVerified) {
            throw new AccessDeniedException("Only verified NGOs can create welfare campaigns.");
        }

        campaign.setId(null);
        campaign.setNgoId(ngoId);
        campaign.setStatus("ACTIVE");
        return campaignRepository.save(campaign);
    }

    @Transactional
    public NGOWelfareCampaign updateCampaign(UUID campaignId, UUID ngoId, NGOWelfareCampaign update) {
        NGOWelfareCampaign existing = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        if (!existing.getNgoId().equals(ngoId)) {
            throw new AccessDeniedException("Unauthorized to modify this campaign");
        }

        if (update.getTitle() != null) existing.setTitle(update.getTitle());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getCoverImageUrl() != null) existing.setCoverImageUrl(update.getCoverImageUrl());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());

        return campaignRepository.save(existing);
    }
}
