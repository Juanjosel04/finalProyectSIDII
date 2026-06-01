package com.uniplan.uniplan_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDetailResponse {

    private String id;
    private String email;
    private String role;
    private String status;

    /* Institutional ID (studentId for STUDENT, employeeId for ORGANIZER/ADMIN) */
    private String institutionalId;

    private String firstName;
    private String lastName;

    /* Student-specific */
    private String program;
    private String campus;

    /* Employee-specific */
    private String employeeType;
    private String contractType;
}
