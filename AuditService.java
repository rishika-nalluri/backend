package com.pawconnect.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(UUID profileId, String action, String entityName, UUID entityId, String details, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .profileId(profileId)
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .timestamp(Instant.now())
                    .build();

            auditRepository.save(auditLog);
            log.debug("Audit record saved: action={}, entity={}/{}", action, entityName, entityId);
        } catch (Exception e) {
            log.warn("Failed to write audit log entry: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForProfile(UUID profileId) {
        return auditRepository.findByProfileIdOrderByTimestampDesc(profileId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsForEntity(String entityName, UUID entityId) {
        return auditRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId);
    }
}
