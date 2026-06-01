package com.uniplan.uniplan_backend.controllers;

import com.uniplan.uniplan_backend.dto.UserProfileResponse;
import com.uniplan.uniplan_backend.model.relational.university.Employee;
import com.uniplan.uniplan_backend.model.relational.university.Student;
import com.uniplan.uniplan_backend.model.relational.uniplan.User;
import com.uniplan.uniplan_backend.repositories.EmployeeRepository;
import com.uniplan.uniplan_backend.repositories.StudentRepository;
import com.uniplan.uniplan_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserRepository     userRepository;
    private final StudentRepository  studentRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus());

        switch (user.getRole()) {
            case "STUDENT" -> {
                builder.institutionalId(user.getStudentId());
                studentRepository.findById(user.getStudentId()).ifPresent(s -> {
                    builder.firstName(s.getFirstName())
                           .lastName(s.getLastName())
                           .campus(s.getCampus() != null ? s.getCampus().toString() : null);
                });
            }
            case "ORGANIZER", "ADMIN" -> {
                builder.institutionalId(user.getEmployeeId());
                if (user.getEmployeeId() != null) {
                    employeeRepository.findById(user.getEmployeeId()).ifPresent(e -> {
                        builder.firstName(e.getFirstName())
                               .lastName(e.getLastName())
                               .contractType(e.getContractType())
                               .employeeType(e.getEmployeeType());
                    });
                }
            }
        }

        return ResponseEntity.ok(builder.build());
    }
}
