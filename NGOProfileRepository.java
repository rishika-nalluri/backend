package com.pawconnect.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NGOProfileRepository extends JpaRepository<NGOProfile, UUID> {
    List<NGOProfile> findByVerificationStatus(String status);
}
