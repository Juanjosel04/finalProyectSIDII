package com.uniplan.uniplan_backend.model.relational.university;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employees", schema = "universidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    @Column(name = "contract_type")
    private String contractType;

    @Column(name = "employee_type")
    private String employeeType;
}