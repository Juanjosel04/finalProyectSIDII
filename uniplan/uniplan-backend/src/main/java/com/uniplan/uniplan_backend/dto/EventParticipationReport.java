package com.uniplan.uniplan_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventParticipationReport {
    private String eventId;
    private String eventCode;
    private String eventTitle;
    private String eventType;
    private String eventStatus;
    private String organizerEmail;
    private String startDate;
    private Integer totalCapacity;
    private Integer registered;
    private Integer attended;
    private Integer waitlist;
    private Integer cancelled;
    private Double attendanceRate;
}
