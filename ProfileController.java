package com.pawconnect.profile;

import com.pawconnect.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        Profile profile = profileService.getProfile(principal.getId()).orElse(null);
        response.put("profile", profile);

        if (profile != null) {
            switch (profile.getRole()) {
                case "VET" -> response.put("vetProfile", profileService.getVetProfile(principal.getId()).orElse(null));
                case "NGO" -> response.put("ngoProfile", profileService.getNGOProfile(principal.getId()).orElse(null));
                default -> response.put("petParentProfile", profileService.getPetParentProfile(principal.getId()).orElse(null));
            }
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<Profile> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Profile updateRequest
    ) {
        Profile updated = profileService.updateProfile(
                principal.getId(),
                updateRequest.getFullName(),
                updateRequest.getPhone(),
                updateRequest.getAvatarUrl()
        );
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/me/vet")
    public ResponseEntity<VetProfile> updateVetDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody VetProfile updateRequest
    ) {
        VetProfile updated = profileService.updateVetProfile(principal.getId(), updateRequest);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/me/ngo")
    public ResponseEntity<NGOProfile> updateNGODetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody NGOProfile updateRequest
    ) {
        NGOProfile updated = profileService.updateNGOProfile(principal.getId(), updateRequest);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/me/pet-parent")
    public ResponseEntity<PetParentProfile> updatePetParentDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody PetParentProfile updateRequest
    ) {
        PetParentProfile updated = profileService.updatePetParentProfile(principal.getId(), updateRequest);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/vets/verified")
    public ResponseEntity<List<VetProfile>> getVerifiedVets() {
        return ResponseEntity.ok(profileService.getVerifiedVets());
    }

    @GetMapping("/ngos/verified")
    public ResponseEntity<List<NGOProfile>> getVerifiedNGOs() {
        return ResponseEntity.ok(profileService.getVerifiedNGOs());
    }
}
