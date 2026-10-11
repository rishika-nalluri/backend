package com.pawconnect.profile;

import com.pawconnect.security.UserPrincipal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Creates the application profile the first time a Supabase account reaches the API. Supabase's database
 * trigger does this on Supabase-hosted databases, but the application database (Aiven) has no such trigger.
 * Vets and NGOs start as PENDING. Admin status is never granted here.
 */
@Service
@RequiredArgsConstructor
public class ProfileProvisioningService {

    private static final Set<String> SELF_SERVICE_ROLES = Set.of("PET_PARENT", "VET", "NGO");

    private final ProfileRepository profiles;
    private final PetParentProfileRepository petParents;
    private final VetProfileRepository vets;
    private final NGOProfileRepository ngos;
    private final TransactionTemplate transactions;

    public Optional<UserPrincipal> principalFor(UUID userId, String email, Map<String, Object> meta) {
        Profile profile = profiles.findById(userId).orElseGet(() -> createOrReload(userId, email, meta));
        return Optional.of(UserPrincipal.builder()
                .id(profile.getId())
                .email(profile.getEmail())
                .fullName(profile.getFullName())
                .role(profile.getRole())
                .build());
    }

    private Profile createOrReload(UUID userId, String email, Map<String, Object> meta) {
        try {
            return transactions.execute(status -> create(userId, email, meta));
        } catch (DataIntegrityViolationException raced) {
            // Two first requests raced. The other one created the profile, so read it.
            return profiles.findById(userId).orElseThrow(() -> raced);
        }
    }

    private Profile create(UUID userId, String email, Map<String, Object> meta) {
        String requested = text(meta, "role");
        String role = requested != null && SELF_SERVICE_ROLES.contains(requested.toUpperCase(Locale.ROOT))
                ? requested.toUpperCase(Locale.ROOT)
                : "PET_PARENT";
        String fullName = text(meta, "full_name") != null ? text(meta, "full_name") : (email == null ? "" : email);

        Profile profile = Profile.builder()
                .id(userId)
                .email(email == null ? null : email.trim().toLowerCase(Locale.ROOT))
                .fullName(fullName)
                .role(role)
                .build();
        profiles.save(profile);

        switch (role) {
            case "VET" -> vets.save(VetProfile.builder()
                    .profileId(userId).profile(profile)
                    .licenseNumber(required(meta, "license_number"))
                    .councilRegistrationNo(required(meta, "council_registration_no"))
                    .specialization(text(meta, "specialization"))
                    .clinicName(text(meta, "clinic_name"))
                    .yearsExperience(years(meta))
                    .verificationStatus("PENDING")
                    .build());
            case "NGO" -> ngos.save(NGOProfile.builder()
                    .profileId(userId).profile(profile)
                    .orgName(required(meta, "org_name"))
                    .registrationNumber(required(meta, "registration_number"))
                    .focusArea(text(meta, "focus_area"))
                    .address(text(meta, "address"))
                    .contactPerson(text(meta, "contact_person") != null ? text(meta, "contact_person") : text(meta, "full_name"))
                    .verificationStatus("PENDING")
                    .build());
            default -> petParents.save(PetParentProfile.builder()
                    .profileId(userId).profile(profile)
                    .address(text(meta, "address"))
                    .city(text(meta, "city"))
                    .pincode(text(meta, "pincode"))
                    .build());
        }
        return profile;
    }

    /** Columns that are NOT NULL in the database: an empty string keeps the sign-up from failing. */
    static String required(Map<String, Object> meta, String key) {
        String value = text(meta, key);
        return value == null ? "" : value;
    }

    static String text(Map<String, Object> meta, String key) {
        if (meta == null || !(meta.get(key) instanceof String s)) {
            return null;
        }
        String trimmed = s.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed;
    }

    static Integer years(Map<String, Object> meta) {
        String raw = text(meta, "years_experience");
        if (raw == null || !raw.matches("\\d{1,2}") ) {
            return null;
        }
        int n = Integer.parseInt(raw);
        return n <= 70 ? n : null;
    }
}
