package com.uniplan.uniplan_backend.model.document.embedded;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
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
     * Human-readable unique code
     * Example: EVT-2025-001
     */
    private String code;

    /*
     * Basic info
     */
    private String title;

    private String description;

    /*
     * ACADEMIC | CULTURAL | SPORT | VOLUNTEER | WORKSHOP | OTHER
     */
    private String type;

    /*
     * ACTIVE | CANCELLED | FINISHED | DRAFT
     */
    private String status;

    /*
     * Date/time info
     */
    private EventSchedule schedule;

    /*
     * Location info
     */
    private EventLocation location;

    /*
     * Capacity and registration counters
     */
    private EventCapacity capacity;

    /*
     * Organizer snapshot (denormalized for read performance)
     */
    private EventOrganizer organizer;

    /*
     * Flexible extra info
     * Examples:
     *   speaker: "Jane Doe"
     *   requirements: "Laptop"
     *   volunteerHours: 2
     *   tags: ["AI", "tecnología"]
     */
    private Map<String, Object> details;

    /*
     * Audit
     */
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}