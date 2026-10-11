package com.pawconnect.audit;

import com.pawconnect.health.ConsentGrant;
import com.pawconnect.ngo.Donation;
import com.pawconnect.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    // Pointcuts
    @Pointcut("execution(* com.pawconnect.health.ConsentService.grantConsent(..))")
    public void consentGrantPointcut() {}

    @Pointcut("execution(* com.pawconnect.health.ConsentService.revokeConsent(..))")
    public void consentRevokePointcut() {}

    @Pointcut("execution(* com.pawconnect.health.HealthRecordService.get*(..))")
    public void healthRecordViewPointcut() {}

    @Pointcut("execution(* com.pawconnect.ngo.DonationService.donate*(..))")
    public void donationPointcut() {}

    // Advice for Grant Consent
    @AfterReturning(pointcut = "consentGrantPointcut()", returning = "result")
    public void auditConsentGrant(JoinPoint jp, Object result) {
        if (result instanceof ConsentGrant grant) {
            auditService.logAction(
                    grant.getPetParentId(),
                    "CONSENT_GRANTED",
                    "ConsentGrant",
                    grant.getId(),
                    String.format("Granted consent for dog %s to vet %s", grant.getDogId(), grant.getVetId()),
                    null
            );
        }
    }

    // Advice for Revoke Consent
    @AfterReturning(pointcut = "consentRevokePointcut()")
    public void auditConsentRevoke(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args.length >= 2 && args[0] instanceof UUID petParentId && args[1] instanceof UUID grantId) {
            auditService.logAction(
                    petParentId,
                    "CONSENT_REVOKED",
                    "ConsentGrant",
                    grantId,
                    "Revoked vet access grant",
                    null
            );
        }
    }

    // Advice for Health Record View by Vet
    @AfterReturning(pointcut = "healthRecordViewPointcut()")
    public void auditHealthRecordView(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args.length >= 3 && args[0] instanceof UUID dogId && args[1] instanceof UUID userId && args[2] instanceof String role) {
            if ("VET".equalsIgnoreCase(role)) {
                auditService.logAction(
                        userId,
                        "RECORD_VIEW",
                        "DogHealthRecord",
                        dogId,
                        String.format("Vet viewed records for dog %s (Method: %s)", dogId, jp.getSignature().getName()),
                        null
                );
            }
        }
    }

    // Advice for Donations
    @AfterReturning(pointcut = "donationPointcut()", returning = "result")
    public void auditDonation(JoinPoint jp, Object result) {
        if (result instanceof Donation donation) {
            auditService.logAction(
                    donation.getPetParentId(),
                    "DONATION_MADE",
                    "Donation",
                    donation.getId(),
                    String.format("Donation of type %s for campaign %s", donation.getDonationType(), donation.getCampaignId()),
                    null
            );
        }
    }
}
