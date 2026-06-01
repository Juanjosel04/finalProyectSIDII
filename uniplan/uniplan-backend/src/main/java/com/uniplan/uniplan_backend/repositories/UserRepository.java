package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.relational.uniplan.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository
        extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(String role);

    long countByRole(String role);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByEmployeeId(String employeeId);
}
