package com.uniplan.uniplan_backend.model.relational.university;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subjects", schema = "universidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subject {

    @Id
    @Column(name = "code")
    private String code;

    private String name;

    @ManyToOne
    @JoinColumn(name = "program_code")
    private Program program;
}