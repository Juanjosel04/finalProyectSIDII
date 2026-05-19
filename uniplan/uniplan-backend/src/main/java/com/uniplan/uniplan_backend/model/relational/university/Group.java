package com.uniplan.uniplan_backend.model.relational.university;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "groups", schema = "universidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @Column(name = "nrc")
    private String nrc;

    private Integer number;

    private String semester;

    @ManyToOne
    @JoinColumn(name = "subject_code")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Employee professor;
}