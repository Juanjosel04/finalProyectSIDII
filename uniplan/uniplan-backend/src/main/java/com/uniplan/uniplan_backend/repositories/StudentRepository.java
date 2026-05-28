package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.relational.university.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    boolean existsByIdAndEmailIgnoreCase(String id, String email);
}
