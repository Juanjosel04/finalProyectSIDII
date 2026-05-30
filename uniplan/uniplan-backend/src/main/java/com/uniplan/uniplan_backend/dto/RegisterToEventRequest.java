package com.uniplan.uniplan_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterToEventRequest {

    /*
     * MongoDB _id del evento al que se inscribe el estudiante.
     * El resto de datos (quién se inscribe) viene del JWT.
     */
    private String eventId;
}
