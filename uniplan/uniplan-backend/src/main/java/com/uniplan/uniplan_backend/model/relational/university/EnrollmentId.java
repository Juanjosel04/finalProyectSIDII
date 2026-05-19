package com.uniplan.uniplan_backend.model.relational.university;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentId implements Serializable {

    private String studentId;

    private String nrc;
}