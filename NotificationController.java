package com.pawconnect.notification;

import com.pawconnect.security.UserPrincipal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.getMyNotifications(principal.getId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Long> res = new HashMap<>();
        res.put("unreadCount", notificationService.getUnreadCount(principal.getId()));
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        notificationService.markAsRead(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.noContent().build();
    }

    // Reminders
    @GetMapping("/reminders")
    public ResponseEntity<List<Reminder>> getMyReminders(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.getMyReminders(principal.getId()));
    }

    @PostMapping("/reminders")
    public ResponseEntity<Reminder> createReminder(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateReminderRequest request
    ) {
        return ResponseEntity.ok(notificationService.createReminder(
                principal.getId(),
                request.getDogId(),
                request.getTitle(),
                request.getType(),
                request.getDueDate()
        ));
    }

    @PatchMapping("/reminders/{id}/complete")
    public ResponseEntity<Void> markReminderComplete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        notificationService.markReminderCompleted(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class CreateReminderRequest {
        private UUID dogId;
        private String title;
        private String type;
        private Instant dueDate;
    }
}
