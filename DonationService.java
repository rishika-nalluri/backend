package com.pawconnect.ngo;

import com.pawconnect.reward.CoinService;
import com.pawconnect.reward.CoinTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonationRedemptionRepository donationRedemptionRepository;
    private final NGOWelfareCampaignRepository campaignRepository;
    private final CoinService coinService;

    @Transactional
    public Donation donateCoins(UUID petParentId, UUID campaignId, int coins, String notes) {
        NGOWelfareCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + campaignId));

        if (coins <= 0) {
            throw new IllegalArgumentException("Donation coins must be positive");
        }

        // Redeem coins from pet parent wallet
        CoinTransaction tx = coinService.redeemCoins(petParentId, coins, "NGO_WELFARE_DONATION", campaignId);

        Donation donation = Donation.builder()
                .petParentId(petParentId)
                .ngoId(campaign.getNgoId())
                .campaignId(campaignId)
                .donationType("COIN")
                .coinsUsed(coins)
                .cashAmount(BigDecimal.valueOf(coins)) // 1 Coin = 1 INR Street dog welfare fund
                .status("COMPLETED")
                .donorNotes(notes)
                .build();

        Donation savedDonation = donationRepository.save(donation);

        // Record redemption linkage
        DonationRedemption redemption = DonationRedemption.builder()
                .donationId(savedDonation.getId())
                .coinTransactionId(tx.getId())
                .build();
        donationRedemptionRepository.save(redemption);

        // Update campaign progress
        campaign.setCoinsCollected(campaign.getCoinsCollected() + coins);
        campaign.setAchievedAmount(campaign.getAchievedAmount().add(BigDecimal.valueOf(coins)));
        campaignRepository.save(campaign);

        log.info("Pet parent {} donated {} PawCoins to campaign {}", petParentId, coins, campaignId);
        return savedDonation;
    }

    @Transactional
    public Donation donateCash(UUID petParentId, UUID campaignId, BigDecimal amount, UUID paymentId, String notes) {
        NGOWelfareCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + campaignId));

        Donation donation = Donation.builder()
                .petParentId(petParentId)
                .ngoId(campaign.getNgoId())
                .campaignId(campaignId)
                .donationType("CASH")
                .cashAmount(amount)
                .paymentId(paymentId)
                .status("COMPLETED")
                .donorNotes(notes)
                .build();

        Donation saved = donationRepository.save(donation);

        campaign.setAchievedAmount(campaign.getAchievedAmount().add(amount));
        campaignRepository.save(campaign);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Donation> getMyDonations(UUID petParentId) {
        return donationRepository.findByPetParentIdOrderByCreatedAtDesc(petParentId);
    }

    @Transactional(readOnly = true)
    public List<Donation> getCampaignDonations(UUID campaignId) {
        return donationRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId);
    }
}
