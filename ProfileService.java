package com.pawconnect.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final VetProfileRepository vetProfileRepository;
    private final NGOProfileRepository ngoProfileRepository;
    private final PetParentProfileRepository petParentProfileRepository;

    @Transactional(readOnly = true)
    public Optional<Profile> getProfile(UUID userId) {
        return profileRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<VetProfile> getVetProfile(UUID userId) {
        return vetProfileRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<NGOProfile> getNGOProfile(UUID userId) {
        return ngoProfileRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<PetParentProfile> getPetParentProfile(UUID userId) {
        return petParentProfileRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public List<VetProfile> getVerifiedVets() {
        return vetProfileRepository.findByVerificationStatus("VERIFIED");
    }

    @Transactional(readOnly = true)
    public List<NGOProfile> getVerifiedNGOs() {
        return ngoProfileRepository.findByVerificationStatus("VERIFIED");
    }

    @Transactional
    public Profile updateProfile(UUID userId, String fullName, String phone, String avatarUrl) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for ID: " + userId));

        if (fullName != null) profile.setFullName(fullName);
        if (phone != null) profile.setPhone(phone);
        if (avatarUrl != null) profile.setAvatarUrl(avatarUrl);

        return profileRepository.save(profile);
    }

    @Transactional
    public VetProfile updateVetProfile(UUID userId, VetProfile updated) {
        VetProfile vet = vetProfileRepository.findById(userId)
                .orElseGet(() -> {
                    Profile profile = profileRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + userId));
                    return VetProfile.builder()
                            .profile(profile)
                            .profileId(userId)
                            .verificationStatus("PENDING")
                            .build();
                });

        if (updated.getLicenseNumber() != null) vet.setLicenseNumber(updated.getLicenseNumber());
        if (updated.getCouncilRegistrationNo() != null) vet.setCouncilRegistrationNo(updated.getCouncilRegistrationNo());
        if (updated.getSpecialization() != null) vet.setSpecialization(updated.getSpecialization());
        if (updated.getClinicName() != null) vet.setClinicName(updated.getClinicName());
        if (updated.getClinicAddress() != null) vet.setClinicAddress(updated.getClinicAddress());
        if (updated.getYearsExperience() != null) vet.setYearsExperience(updated.getYearsExperience());
        if (updated.getConsultationFee() != null) vet.setConsultationFee(updated.getConsultationFee());
        if (updated.getBio() != null) vet.setBio(updated.getBio());
        if (updated.getVerificationDocUrl() != null) vet.setVerificationDocUrl(updated.getVerificationDocUrl());

        return vetProfileRepository.save(vet);
    }

    @Transactional
    public NGOProfile updateNGOProfile(UUID userId, NGOProfile updated) {
        NGOProfile ngo = ngoProfileRepository.findById(userId)
                .orElseGet(() -> {
                    Profile profile = profileRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + userId));
                    return NGOProfile.builder()
                            .profile(profile)
                            .profileId(userId)
                            .verificationStatus("PENDING")
                            .build();
                });

        if (updated.getOrgName() != null) ngo.setOrgName(updated.getOrgName());
        if (updated.getRegistrationNumber() != null) ngo.setRegistrationNumber(updated.getRegistrationNumber());
        if (updated.getRegistrationCertUrl() != null) ngo.setRegistrationCertUrl(updated.getRegistrationCertUrl());
        if (updated.getFocusArea() != null) ngo.setFocusArea(updated.getFocusArea());
        if (updated.getAddress() != null) ngo.setAddress(updated.getAddress());
        if (updated.getCity() != null) ngo.setCity(updated.getCity());
        if (updated.getPincode() != null) ngo.setPincode(updated.getPincode());
        if (updated.getContactPerson() != null) ngo.setContactPerson(updated.getContactPerson());
        if (updated.getWebsiteUrl() != null) ngo.setWebsiteUrl(updated.getWebsiteUrl());
        if (updated.getBio() != null) ngo.setBio(updated.getBio());

        return ngoProfileRepository.save(ngo);
    }

    @Transactional
    public PetParentProfile updatePetParentProfile(UUID userId, PetParentProfile updated) {
        PetParentProfile parent = petParentProfileRepository.findById(userId)
                .orElseGet(() -> {
                    Profile profile = profileRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + userId));
                    return PetParentProfile.builder()
                            .profile(profile)
                            .profileId(userId)
                            .build();
                });

        if (updated.getAddress() != null) parent.setAddress(updated.getAddress());
        if (updated.getCity() != null) parent.setCity(updated.getCity());
        if (updated.getPincode() != null) parent.setPincode(updated.getPincode());
        if (updated.getEmergencyContact() != null) parent.setEmergencyContact(updated.getEmergencyContact());

        return petParentProfileRepository.save(parent);
    }
}
