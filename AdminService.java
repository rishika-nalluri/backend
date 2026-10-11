package com.pawconnect.admin;

import com.pawconnect.audit.AuditService;
import com.pawconnect.notification.NotificationService;
import com.pawconnect.security.CurrentUser;
import com.pawconnect.storage.SupabaseStorage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Admin review of vet and NGO registrations. Admin status is the is_admin flag on profiles. */
@Service
public class AdminService {

    private final JdbcTemplate jdbc;
    private final SupabaseStorage storage;
    private final NotificationService notifications;
    private final AuditService audit;

    public AdminService(JdbcTemplate jdbc, SupabaseStorage storage, NotificationService notifications,
                        AuditService audit) {
        this.jdbc = jdbc;
        this.storage = storage;
        this.notifications = notifications;
        this.audit = audit;
    }

    public List<Map<String, Object>> pendingVerifications(CurrentUser admin) {
        requireAdmin(admin);
        return jdbc.queryForList(
                "select p.id, p.email, p.full_name, p.role::text as role, v.clinic_name as organisation, "
                        + "v.license_number as registration_number, v.verification_doc_url as document_path "
                        + "from public.profiles p join public.vet_profiles v on v.profile_id = p.id "
                        + "where v.verification_status = 'PENDING' "
                        + "union all "
                        + "select p.id, p.email, p.full_name, p.role::text, n.org_name, n.registration_number, "
                        + "n.registration_cert_url "
                        + "from public.profiles p join public.ngo_profiles n on n.profile_id = p.id "
                        + "where n.verification_status = 'PENDING' "
                        + "order by full_name");
    }

    public String documentUrl(CurrentUser admin, UUID profileId) {
        requireAdmin(admin);
        String path = jdbc.queryForList(
                        "select verification_doc_url from public.vet_profiles where profile_id = ? "
                                + "union all select registration_cert_url from public.ngo_profiles where profile_id = ?",
                        String.class, profileId, profileId)
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No document uploaded"));
        return storage.signedUrl(path);
    }

    @Transactional
    public void decide(CurrentUser admin, UUID profileId, String decision) {
        requireAdmin(admin);
        if (!"VERIFIED".equals(decision) && !"REJECTED".equals(decision)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be VERIFIED or REJECTED");
        }
        int updated = jdbc.update(
                "update public.vet_profiles set verification_status = cast(? as public.verification_status) "
                        + "where profile_id = ?",
                decision, profileId);
        if (updated == 0) {
            updated = jdbc.update(
                    "update public.ngo_profiles set verification_status = cast(? as public.verification_status) "
                            + "where profile_id = ?",
                    decision, profileId);
        }
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No vet or NGO account with that id");
        }

        boolean approved = "VERIFIED".equals(decision);
        notifications.sendNotification(
                profileId,
                approved ? "Your account is approved" : "Your registration was not approved",
                approved
                        ? "Your PawConnect registration is approved. You can now use all features."
                        : "Your registration was not approved. Please contact support with corrected documents.",
                "VERIFICATION",
                null);
        audit.logAction(admin.id(), "VERIFICATION_DECISION", "profiles", profileId, decision, null);
    }

    private void requireAdmin(CurrentUser user) {
        Boolean isAdmin = jdbc.queryForObject("select is_admin from public.profiles where id = ?",
                Boolean.class, user.id());
        if (!Boolean.TRUE.equals(isAdmin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
