package com.uniplan.uniplan_backend.model.relational.uniplan;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "volunteer_hours", schema = "uniplan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerHours {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "student_user_id")
    private User studentUser;

    @Column(name = "completed_hours")
    private Integer completedHours;
}