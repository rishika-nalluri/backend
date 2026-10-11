package com.pawconnect.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByProfileIdOrderByCreatedAtDesc(UUID profileId);
    Optional<Payment> findByGatewayRef(String gatewayRef);
    java.util.Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(java.util.UUID orderId);
}
