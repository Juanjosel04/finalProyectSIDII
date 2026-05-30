package com.uniplan.uniplan_backend.model.document;

import com.uniplan.uniplan_backend.model.document.embedded.StudentSnapshot;
import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MongoDB document: event_registrations
 *
 * Stores all event registrations with a student snapshot
 * so each document is self-contained.
 */
@Document(collection = "event_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationDocument {

    @Id
    private String id;

    /*
     * MongoDB _id of the event
     */
    private String eventId;

    /*
     * Human-readable event code (EVT-2025-001)
     */
    private String eventCode;

    /*
     * Student info snapshot at registration time
     */
    private StudentSnapshot student;

    /*
     * REGISTERED | CANCELLED | REJECTED | ATTENDED | WAITLIST
     */
    private String status;

    /*
     * Snapshot of validation data at registration time
     * Example: { semester: 4, program: "Ingeniería de Sistemas", eligible: true }
     */
    private Map<String, Object> validationSnapshot;

    /*
     * Lifecycle timestamps
     */
    private LocalDateTime registeredAt;

    private LocalDateTime cancelledAt;

    private String cancellationReason;

    private LocalDateTime rejectedAt;

    private String rejectionReason;

    /*
     * Audit
     */
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
