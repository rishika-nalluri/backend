package com.pawconnect.dog;

import com.pawconnect.health.ConsentGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DogService {

    private final DogRepository dogRepository;
    private final ConsentGrantRepository consentGrantRepository;

    @Transactional(readOnly = true)
    public List<Dog> getMyDogs(UUID petParentId) {
        return dogRepository.findByPetParentIdAndIsActiveTrue(petParentId);
    }

    @Transactional(readOnly = true)
    public Dog getDogDetails(UUID dogId, UUID currentUserId, String role) {
        Dog dog = dogRepository.findById(dogId)
                .orElseThrow(() -> new IllegalArgumentException("Dog not found with ID: " + dogId));

        if ("PET_PARENT".equalsIgnoreCase(role)) {
            if (!dog.getPetParentId().equals(currentUserId)) {
                throw new AccessDeniedException("You do not own this dog profile.");
            }
            return dog;
        } else if ("VET".equalsIgnoreCase(role)) {
            boolean hasConsent = consentGrantRepository.findActiveGrant(dogId, currentUserId).isPresent();
            if (!hasConsent) {
                throw new AccessDeniedException("Access denied: Active consent grant from pet parent is required to view this dog's records.");
            }
            return dog;
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            return dog;
        }

        throw new AccessDeniedException("Unauthorized to access dog profile.");
    }

    @Transactional
    public Dog createDog(UUID petParentId, Dog dog) {
        dog.setId(null);
        dog.setPetParentId(petParentId);
        dog.setIsActive(true);
        return dogRepository.save(dog);
    }

    @Transactional
    public Dog updateDog(UUID dogId, UUID petParentId, Dog update) {
        Dog existing = dogRepository.findByIdAndPetParentId(dogId, petParentId)
                .orElseThrow(() -> new IllegalArgumentException("Dog not found or unauthorized"));

        if (update.getName() != null) existing.setName(update.getName());
        if (update.getBreed() != null) existing.setBreed(update.getBreed());
        if (update.getGender() != null) existing.setGender(update.getGender());
        if (update.getDob() != null) existing.setDob(update.getDob());
        if (update.getWeight() != null) existing.setWeight(update.getWeight());
        if (update.getMicrochipNumber() != null) existing.setMicrochipNumber(update.getMicrochipNumber());
        if (update.getAllergies() != null) existing.setAllergies(update.getAllergies());
        if (update.getPhotoUrl() != null) existing.setPhotoUrl(update.getPhotoUrl());

        return dogRepository.save(existing);
    }

    @Transactional
    public void deleteDog(UUID dogId, UUID petParentId) {
        Dog existing = dogRepository.findByIdAndPetParentId(dogId, petParentId)
                .orElseThrow(() -> new IllegalArgumentException("Dog not found or unauthorized"));
        existing.setIsActive(false);
        dogRepository.save(existing);
    }

    /** 404 (not 403) for someone else's dog, so ids cannot be probed. */
    public void assertOwned(java.util.UUID dogId, java.util.UUID ownerId) {
        if (dogRepository.findByIdAndPetParentId(dogId, ownerId).isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Dog not found");
        }
    }
}
