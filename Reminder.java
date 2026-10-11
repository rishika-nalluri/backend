package com.pawconnect.notification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reminders", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "dog_id", nullable = false)
    private UUID dogId;

    @Column(name = "pet_parent_id", nullable = false)
    private UUID petParentId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // VACCINATION, MEDICATION, APPOINTMENT, DEWORMING, GENERAL

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Builder.Default
    @Column(name = "is_sent", nullable = false)
    private Boolean isSent = false;

    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
