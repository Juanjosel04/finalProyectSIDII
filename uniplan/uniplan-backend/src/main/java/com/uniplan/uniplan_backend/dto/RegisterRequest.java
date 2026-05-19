package com.uniplan.uniplan_backend.dto;

import lombok.Getter;

import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String email;

    private String password;
    private String role;
    private String studentId;
    private String employeeId;
}