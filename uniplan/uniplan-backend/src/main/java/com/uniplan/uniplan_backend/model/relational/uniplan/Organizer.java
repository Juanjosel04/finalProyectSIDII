package com.uniplan.uniplan_backend.model.relational.uniplan;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "organizers", schema = "uniplan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Organizer {

    @Id
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "organizer_type")
    private String organizerType;

    private String faculty;

    private String department;

    private String specialization;

    @Column(name = "academic_program")
    private String academicProgram;

    private Integer semester;

    @Column(name = "association_name")
    private String associationName;

    @Column(name = "administrative_area")
    private String administrativeArea;

    private String position;
}