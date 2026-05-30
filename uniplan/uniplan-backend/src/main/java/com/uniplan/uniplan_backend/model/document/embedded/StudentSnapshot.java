package com.uniplan.uniplan_backend.model.document.embedded;

import lombok.*;

/**
 * Snapshot of student data at the time of registration.
 * Stored inside event_registrations and attendance_records
 * so the document is self-contained even if the student changes later.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSnapshot {

    /*
     * UUID of the User in PostgreSQL
     */
    private String userId;

    /*
     * Student institutional ID
     */
    private String studentId;

    private String firstName;

    private String lastName;

    private String email;

    /*
     * Academic program at registration time
     */
    private String program;

    /*
     * Semester at registration time
     */
    private Integer semester;
}
