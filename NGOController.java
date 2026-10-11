package com.pawconnect.ngo;

import com.pawconnect.security.UserPrincipal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ngo")
@RequiredArgsConstructor
public class NGOController {

    private final NGOCampaignService campaignService;
    private final DonationService donationService;
    private final ImpactReportService impactReportService;

    // Public Campaigns
    @GetMapping("/campaigns")
    public ResponseEntity<List<NGOWelfareCampaign>> getCampaigns() {
        return ResponseEntity.ok(campaignService.getActiveCampaigns());
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<NGOWelfareCampaign> getCampaignById(@PathVariable UUID id) {
        return campaignService.getCampaign(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // NGO Campaign Management
    @PostMapping("/campaigns")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ResponseEntity<NGOWelfareCampaign> createCampaign(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody NGOWelfareCampaign campaign
    ) {
        return ResponseEntity.ok(campaignService.createCampaign(principal.getId(), campaign));
    }

    @GetMapping("/my-campaigns")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ResponseEntity<List<NGOWelfareCampaign>> getMyCampaigns(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(campaignService.getMyCampaigns(principal.getId()));
    }

    // Donations
    @PostMapping("/campaigns/{id}/donate-coins")
    public ResponseEntity<Donation> donateCoins(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody DonateCoinsRequest request
    ) {
        return ResponseEntity.ok(donationService.donateCoins(
                principal.getId(),
                id,
                request.getCoins(),
                request.getNotes()
        ));
    }

    @PostMapping("/campaigns/{id}/donate-cash")
    public ResponseEntity<Donation> donateCash(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody DonateCashRequest request
    ) {
        return ResponseEntity.ok(donationService.donateCash(
                principal.getId(),
                id,
                request.getAmount(),
                request.getPaymentId(),
                request.getNotes()
        ));
    }

    @GetMapping("/my-donations")
    public ResponseEntity<List<Donation>> getMyDonations(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(donationService.getMyDonations(principal.getId()));
    }

    // Impact Reports
    @GetMapping("/impact-reports")
    public ResponseEntity<List<ImpactReport>> getAllImpactReports() {
        return ResponseEntity.ok(impactReportService.getAllReports());
    }

    @GetMapping("/campaigns/{id}/impact-reports")
    public ResponseEntity<List<ImpactReport>> getCampaignReports(@PathVariable UUID id) {
        return ResponseEntity.ok(impactReportService.getReportsForCampaign(id));
    }

    @PostMapping("/impact-reports")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ResponseEntity<ImpactReport> postImpactReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ImpactReport report
    ) {
        return ResponseEntity.ok(impactReportService.postImpactReport(principal.getId(), report));
    }

    @Data
    public static class DonateCoinsRequest {
        private int coins;
        private String notes;
    }

    @Data
    public static class DonateCashRequest {
        private BigDecimal amount;
        private UUID paymentId;
        private String notes;
    }
}
