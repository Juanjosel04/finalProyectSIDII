package com.uniplan.uniplan_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventListResponse {

    private String id;

    private String code;

    private String title;

    private String type;

    private String status;

    /*
     * Flattened from schedule.startDate for convenience
     */
    private LocalDateTime startDate;

    /*
     * Flattened from location.venue for convenience
     */
    private String venue;

    /*
     * Flattened from location.modality (VIRTUAL | IN_PERSON | HYBRID)
     */
    private String modality;

    /*
     * Flattened from capacity.total
     */
    private Integer totalCapacity;

    /*
     * Flattened from capacity.available
     */
    private Integer availableSpots;
}