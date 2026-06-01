package com.uniplan.uniplan_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {

    private String email;
    private String role;
    private String status;

    /* Institutional data */
    private String firstName;
    private String lastName;
    private String institutionalId;

    /* Student-specific */
    private String program;
    private String campus;

    /* Employee-specific */
    private String contractType;
    private String employeeType;
}
