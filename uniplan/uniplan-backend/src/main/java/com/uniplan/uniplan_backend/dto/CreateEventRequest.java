package com.uniplan.uniplan_backend.dto;

import com.uniplan.uniplan_backend.model.document.embedded.EventCapacity;
import com.uniplan.uniplan_backend.model.document.embedded.EventLocation;
import com.uniplan.uniplan_backend.model.document.embedded.EventOrganizer;
import com.uniplan.uniplan_backend.model.document.embedded.EventSchedule;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CreateEventRequest {

    private String title;

    private String description;

    /*
     * ACADEMIC | CULTURAL | SPORT | VOLUNTEER | WORKSHOP | OTHER
     */
    private String type;

    private EventSchedule schedule;

    private EventLocation location;

    private EventCapacity capacity;

    private EventOrganizer organizer;

    /*
     * Flexible extra info (tags, speaker, requirements, volunteerHours, etc.)
     */
    private Map<String, Object> details;
}