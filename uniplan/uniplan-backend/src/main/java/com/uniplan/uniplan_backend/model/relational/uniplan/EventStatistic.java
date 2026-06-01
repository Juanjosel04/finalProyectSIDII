package com.uniplan.uniplan_backend.model.relational.uniplan;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * PostgreSQL table: uniplan.event_statistics
 *
 * Almacena estadísticas agregadas de cada evento.
 * No forma parte del modelo transaccional principal.
 * Se sincroniza desde MongoDB después de cada mutación
 * y mediante un job horario.
 */
@Entity
@Table(name = "event_statistics", schema = "uniplan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventStatistic {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_code")
    private String eventCode;

    @Column(name = "event_title")
    private String eventTitle;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "event_status")
    private String eventStatus;

    @Column(name = "total_capacity")
    private Integer totalCapacity;

    @Column(nullable = false)
    @Builder.Default
    private Integer registered = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer cancelled = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer attended = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer waitlist = 0;

    /*
     * (inscritos + asistidos) / capacidad_total * 100
     */
    @Column(name = "occupancy_percentage")
    @Builder.Default
    private Double occupancyPercentage = 0.0;

    /*
     * asistidos / (inscritos + asistidos) * 100
     */
    @Column(name = "attendance_rate")
    @Builder.Default
    private Double attendanceRate = 0.0;

    @Column(name = "organizer_email")
    private String organizerEmail;

    @Column(name = "event_start_date")
    private LocalDateTime eventStartDate;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
}
