package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.relational.uniplan.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository
        extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByEmployeeId(String employeeId);
}
