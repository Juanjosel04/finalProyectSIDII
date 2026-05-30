package com.uniplan.uniplan_backend.model.document;

import com.uniplan.uniplan_backend.model.document.embedded.InstitutionalRef;
import lombok.*;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document: uniplan_users
 *
 * Mirrors the PostgreSQL uniplan.users table but lives in Mongo
 * to allow flexible fields (institutional_ref, organizer_type).
 * The source of truth for authentication remains PostgreSQL.
 * This document is used for queries, dashboards, and denormalized reads.
 */
@Document(collection = "uniplan_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniplanUserDocument {

    @Id
    private String id;

    /*
     * UUID from PostgreSQL uniplan.users (foreign key bridge)
     */
    private String postgresUserId;

    private String username;

    private String email;

    /*
     * STUDENT | ORGANIZER | ADMIN
     */
    private String role;

    /*
     * ACTIVE | INACTIVE | SUSPENDED
     */
    private String status;

    /*
     * Reference to universidad schema record
     */
    private InstitutionalRef institutionalRef;

    /*
     * Only populated when role = ORGANIZER
     * STUDENT_ORG | FACULTY | ADMINISTRATIVE
     */
    private String organizerType;

    /*
     * Audit
     */
    private LocalDateTime createdAt;
}
