package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.relational.university.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    boolean existsByIdAndEmailIgnoreCase(String id, String email);
}
