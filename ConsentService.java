package com.pawconnect.health;

import com.pawconnect.dog.Dog;
import com.pawconnect.dog.DogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private final ConsentGrantRepository consentGrantRepository;
    private final DogRepository dogRepository;

    @Transactional
    public ConsentGrant grantConsent(UUID petParentId, UUID dogId, UUID vetId, Instant expiresAt, String notes) {
        Dog dog = dogRepository.findByIdAndPetParentId(dogId, petParentId)
                .orElseThrow(() -> new AccessDeniedException("You do not own this dog profile."));

        ConsentGrant grant = ConsentGrant.builder()
                .dogId(dog.getId())
                .petParentId(petParentId)
                .vetId(vetId)
                .expiresAt(expiresAt)
                .notes(notes)
                .build();

        return consentGrantRepository.save(grant);
    }

    @Transactional
    public void revokeConsent(UUID petParentId, UUID grantId) {
        ConsentGrant grant = consentGrantRepository.findById(grantId)
                .orElseThrow(() -> new IllegalArgumentException("Consent grant not found"));

        if (!grant.getPetParentId().equals(petParentId)) {
            throw new AccessDeniedException("Unauthorized to revoke this consent grant.");
        }

        grant.setRevokedAt(Instant.now());
        consentGrantRepository.save(grant);
    }

    @Transactional(readOnly = true)
    public List<ConsentGrant> getGrantsForDog(UUID petParentId, UUID dogId) {
        dogRepository.findByIdAndPetParentId(dogId, petParentId)
                .orElseThrow(() -> new AccessDeniedException("You do not own this dog profile."));
        return consentGrantRepository.findByDogId(dogId);
    }

    @Transactional(readOnly = true)
    public List<ConsentGrant> getGrantsForVet(UUID vetId) {
        return consentGrantRepository.findByVetIdAndRevokedAtIsNull(vetId);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveConsent(UUID dogId, UUID vetId) {
        return consentGrantRepository.findActiveGrant(dogId, vetId).isPresent();
    }
}
