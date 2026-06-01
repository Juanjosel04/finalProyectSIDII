package com.uniplan.uniplan_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttendanceRequest {

    private String eventId;

    /*
     * Código/ID institucional del estudiante (ej. A00123456)
     */
    private String studentCode;
}
