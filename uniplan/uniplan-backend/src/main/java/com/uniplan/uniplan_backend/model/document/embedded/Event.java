package com.uniplan.uniplan_backend.model.document.embedded;

import lombok.*;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import java.util.List;

import java.util.Map;



@Document(collection = "events")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    private String id;



    /*
     * Basic info
     */

    private String title;

    private String description;

    private String type;



    /*
     * Event location
     */

    private String location;



    /*
     * Event capacity
     */

    private Integer capacity;



    /*
     * Dates
     */

    private LocalDateTime startDate;

    private LocalDateTime endDate;



    /*
     * Organizer
     */

    private String organizerId;



    /*
     * ACTIVE
     * CANCELLED
     * FINISHED
     */

    private String status;



    /*
     * Audit
     */

    private LocalDateTime createdAt;



    /*
     * Tags
     */

    private List<String> tags;



    /*
     * Flexible metadata
     *
     * Examples:
     * speaker
     * sport
     * hours
     * requirements
     */

    private Map<String, Object> metadata;
}