package com.uniplan.uniplan_backend.model.document.embedded;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventOrganizer {

    /*
     * UUID of the organizer user (PostgreSQL reference)
     */
    private String userId;

    /*
     * Display name (snapshot at creation time)
     */
    private String name;

    /*
     * Organizer email (snapshot)
     */
    private String email;

    /*
     * STUDENT | EMPLOYEE | FACULTY | ADMIN
     */
    private String organizerType;

    /*
     * Faculty or department
     */
    private String faculty;

    /*
     * Association or club name (if applicable)
     */
    private String associationName;
}
