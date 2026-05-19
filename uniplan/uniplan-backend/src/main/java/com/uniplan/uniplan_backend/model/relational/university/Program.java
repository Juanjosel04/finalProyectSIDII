package com.uniplan.uniplan_backend.model.relational.university;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "programs", schema = "universidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Program {

    @Id
    @Column(name = "code")
    private Integer code;

    private String name;
}