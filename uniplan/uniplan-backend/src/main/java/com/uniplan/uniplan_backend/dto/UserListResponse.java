package com.uniplan.uniplan_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {

    private String id;
    private String email;
    private String role;
    private String status;
    private String employeeId;
}
