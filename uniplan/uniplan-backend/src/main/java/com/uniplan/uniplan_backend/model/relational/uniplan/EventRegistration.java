package com.uniplan.uniplan_backend.model.relational.uniplan;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "event_registrations", schema = "uniplan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistration {

    @Id
    private UUID id;

    @Column(name = "event_mongo_id")
    private String eventMongoId;

    @Column(name = "event_code")
    private String eventCode;

    @ManyToOne
    @JoinColumn(name = "student_user_id")
    private User studentUser;

    private String status;
}