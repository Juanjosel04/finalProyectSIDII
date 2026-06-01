package com.uniplan.uniplan_backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrganizerActivityReport {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String employeeId;
    private long eventsCount;
    private long totalRegistered;
    private long totalAttended;
    private long totalCancelled;
}
