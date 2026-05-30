package com.uniplan.uniplan_backend.model.document.embedded;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSchedule {

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    /*
     * Duration in minutes
     * Calculated field: endDate - startDate
     */
    private Integer durationMinutes;

    /*
     * Timezone
     * Example: America/Bogota
     */
    private String timezone;
}
