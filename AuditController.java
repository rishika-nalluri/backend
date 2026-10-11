package com.pawconnect.audit;

import com.pawconnect.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/my-logs")
    public ResponseEntity<List<AuditLog>> getMyAuditLogs(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(auditService.getLogsForProfile(principal.getId()));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<AuditLog>> getEntityAuditLogs(
            @PathVariable String entityName,
            @PathVariable UUID entityId
    ) {
        return ResponseEntity.ok(auditService.getLogsForEntity(entityName, entityId));
    }
}
