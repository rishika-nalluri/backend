package com.pawconnect.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ReminderRepository reminderRepository;

    @Transactional
    public Notification sendNotification(UUID profileId, String title, String body, String type, UUID referenceId) {
        Notification notification = Notification.builder()
                .profileId(profileId)
                .title(title)
                .body(body)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications(UUID profileId) {
        return notificationRepository.findByProfileIdOrderByCreatedAtDesc(profileId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID profileId) {
        return notificationRepository.countByProfileIdAndIsReadFalse(profileId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID profileId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getProfileId().equals(profileId)) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllAsRead(UUID profileId) {
        List<Notification> unread = notificationRepository.findByProfileIdAndIsReadFalseOrderByCreatedAtDesc(profileId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    // Reminders
    @Transactional
    public Reminder createReminder(UUID petParentId, UUID dogId, String title, String type, Instant dueDate) {
        Reminder r = Reminder.builder()
                .petParentId(petParentId)
                .dogId(dogId)
                .title(title)
                .type(type)
                .dueDate(dueDate)
                .isSent(false)
                .isCompleted(false)
                .build();
        return reminderRepository.save(r);
    }

    @Transactional(readOnly = true)
    public List<Reminder> getMyReminders(UUID petParentId) {
        return reminderRepository.findByPetParentIdOrderByDueDateAsc(petParentId);
    }

    @Transactional
    public void markReminderCompleted(UUID reminderId, UUID petParentId) {
        reminderRepository.findById(reminderId).ifPresent(r -> {
            if (r.getPetParentId().equals(petParentId)) {
                r.setIsCompleted(true);
                reminderRepository.save(r);
            }
        });
    }
}
