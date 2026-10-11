package com.pawconnect.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderSchedulerService {

    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;

    // Checks every 15 minutes for pending due reminders
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void processDueReminders() {
        Instant now = Instant.now();
        List<Reminder> dueReminders = reminderRepository.findByIsSentFalseAndDueDateBefore(now);

        if (!dueReminders.isEmpty()) {
            log.info("Processing {} due reminders...", dueReminders.size());
            for (Reminder reminder : dueReminders) {
                try {
                    String title = "Upcoming Care Reminder: " + reminder.getTitle();
                    String body = String.format("A scheduled %s task for your dog is due today. Tap to view your dog's health dossier.", reminder.getType());

                    notificationService.sendNotification(
                            reminder.getPetParentId(),
                            title,
                            body,
                            "REMINDER",
                            reminder.getDogId()
                    );

                    reminder.setIsSent(true);
                    reminderRepository.save(reminder);
                } catch (Exception e) {
                    log.error("Failed to process reminder {}: {}", reminder.getId(), e.getMessage());
                }
            }
        }
    }
}
