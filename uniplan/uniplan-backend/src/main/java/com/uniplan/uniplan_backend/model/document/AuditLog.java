package com.uniplan.uniplan_backend.model.document;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MongoDB document: audit_logs
 *
 * Immutable log of every significant action in the system.
 * Never updated after creation — append-only.
 */
@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    /*
     * Type of entity affected
     * Example: EVENT | USER | REGISTRATION | ATTENDANCE | ORGANIZER
     */
    @Indexed
    private String entity;

    /*
     * MongoDB _id or UUID of the affected entity
     */
    private String entityId;

    /*
     * Human-readable code (EVT-2025-001, etc.)
     */
    private String entityCode;

    /*
     * Action performed
     * Example: CREATE | UPDATE | CANCEL | REGISTER | CHECK_IN | CHECK_OUT | DEACTIVATE
     */
    @Indexed
    private String action;

    /*
     * Who performed the action
     * { userId, email, role }
     */
    private Map<String, Object> performedBy;

    /*
     * Target of the action (if different from entity)
     * Example: when admin deactivates an organizer:
     *   entity = USER, target = { organizerId, name }
     */
    private Map<String, Object> target;

    /*
     * Field-level changes
     * Example: { status: { from: "ACTIVE", to: "CANCELLED" } }
     */
    private Map<String, Object> changes;

    /*
     * Additional context
     * Example: { ip: "192.168.1.1", userAgent: "..." }
     */
    private Map<String, Object> metadata;

    /*
     * Audit — no updatedAt, logs are immutable
     */
    @Indexed
    private LocalDateTime createdAt;
}
