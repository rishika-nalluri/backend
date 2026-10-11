package com.pawconnect.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment recordPayment(UUID profileId, UUID orderId, BigDecimal amount, String gatewayRef, String method) {
        Payment payment = Payment.builder()
                .profileId(profileId)
                .orderId(orderId)
                .amount(amount)
                .gatewayRef(gatewayRef)
                .paymentMethod(method)
                .status("COMPLETED")
                .paidAt(Instant.now())
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> getMyPayments(UUID profileId) {
        return paymentRepository.findByProfileIdOrderByCreatedAtDesc(profileId);
    }
}
