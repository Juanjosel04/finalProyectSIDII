package com.uniplan.uniplan_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminRegisterRequest {

    private String eventId;
    private String studentEmail;
}
