package com.uniplan.uniplan_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponse {

    private String id;

    private String eventId;

    private String eventCode;

    /*
     * REGISTERED | CANCELLED | REJECTED | ATTENDED | WAITLIST
     */
    private String status;

    private LocalDateTime registeredAt;

    private LocalDateTime cancelledAt;

    private String cancellationReason;

    /*
     * Nombre del evento (snapshot para mostrar en el dashboard)
     */
    private String eventTitle;

    /*
     * Cupos disponibles restantes después de la inscripción
     */
    private Integer availableSpotsAfter;
}
