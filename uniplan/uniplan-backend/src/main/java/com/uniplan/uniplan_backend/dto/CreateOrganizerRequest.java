package com.uniplan.uniplan_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrganizerRequest {

    private String email;
    private String password;
    private String employeeId;
}
