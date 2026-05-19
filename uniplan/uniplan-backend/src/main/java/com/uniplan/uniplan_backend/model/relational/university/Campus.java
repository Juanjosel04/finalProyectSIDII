package com.uniplan.uniplan_backend.model.relational.university;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campuses", schema = "universidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Campus {

    @Id
    @Column(name = "code")
    private Integer code;

    private String name;
}