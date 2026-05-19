package com.uniplan.uniplan_backend.model.relational.university;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "enrollments", schema = "universidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @EmbeddedId
    private EnrollmentId id;

    @ManyToOne
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @MapsId("nrc")
    @JoinColumn(name = "nrc")
    private Group group;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    private String status;
}