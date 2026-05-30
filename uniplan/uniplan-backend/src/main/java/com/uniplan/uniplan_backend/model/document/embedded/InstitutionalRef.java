package com.uniplan.uniplan_backend.model.document.embedded;

import lombok.*;

/**
 * Reference to the institutional database (schema universidad in PostgreSQL).
 * Stored inside UniplanUserDocument to link the platform user
 * with their record in the university system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionalRef {

    /*
     * STUDENT | EMPLOYEE
     */
    private String type;

    /*
     * studentId or employeeId from universidad schema
     */
    private String institutionalId;

    /*
     * Snapshot of name at registration time
     */
    private String firstName;

    private String lastName;

    /*
     * Faculty / department (for employees)
     */
    private String faculty;

    /*
     * Academic program (for students)
     */
    private String program;
}
