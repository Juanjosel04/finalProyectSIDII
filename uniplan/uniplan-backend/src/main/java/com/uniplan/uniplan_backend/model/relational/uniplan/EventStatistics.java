package com.uniplan.uniplan_backend.model.relational.uniplan;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "event_statistics", schema = "uniplan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventStatistics {

    @Id
    private UUID id;

    @Column(name = "event_mongo_id")
    private String eventMongoId;

    @Column(name = "event_code")
    private String eventCode;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "total_registered")
    private Integer totalRegistered;

    @Column(name = "total_cancelled")
    private Integer totalCancelled;

    @Column(name = "total_attended")
    private Integer totalAttended;

    @Column(name = "occupancy_percentage")
    private BigDecimal occupancyPercentage;
}