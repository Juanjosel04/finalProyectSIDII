package com.uniplan.uniplan_backend.controllers;

import com.uniplan.uniplan_backend.dto.CreateOrganizerRequest;
import com.uniplan.uniplan_backend.dto.UserListResponse;
import com.uniplan.uniplan_backend.model.relational.uniplan.User;
import com.uniplan.uniplan_backend.repositories.UserRepository;
import com.uniplan.uniplan_backend.services.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserAdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUserCount() {
        long total      = userRepository.count();
        long organizers = userRepository.countByRole("ORGANIZER");
        long students   = userRepository.countByRole("STUDENT");
        return ResponseEntity.ok(Map.of(
                "total",      total,
                "organizers", organizers,
                "students",   students
        ));
    }

    @GetMapping("/organizers")
    public ResponseEntity<List<UserListResponse>> getOrganizers() {
        List<UserListResponse> list = userRepository.findByRole("ORGANIZER")
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/organizers")
    public ResponseEntity<?> createOrganizer(@RequestBody CreateOrganizerRequest req, Principal principal) {

        String email = req.getEmail() == null ? null : req.getEmail().trim().toLowerCase();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo es obligatorio"));
        }
        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es obligatoria"));
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe una cuenta con ese correo"));
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role("ORGANIZER")
                .employeeId(req.getEmployeeId() != null ? req.getEmployeeId().trim() : null)
                .status("ACTIVE")
                .build();

        User saved = userRepository.save(user);

        try {
            Map<String, Object> performedBy = buildPerformedBy(principal);
            auditService.log(
                    "USER", saved.getId().toString(), saved.getEmail(),
                    "CREATE_ORGANIZER",
                    performedBy,
                    Map.of("email", saved.getEmail(), "role", "ORGANIZER"),
                    null
            );
        } catch (Exception ex) {
            System.err.println("[UserAdminController] Audit error on createOrganizer: " + ex.getMessage());
        }

        return ResponseEntity.ok(toResponse(saved));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateOrganizer(@PathVariable String id, Principal principal) {
        try {
            User user = userRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            user.setStatus("INACTIVE");
            User saved = userRepository.save(user);

            try {
                Map<String, Object> performedBy = buildPerformedBy(principal);
                auditService.log(
                        "USER", id, null,
                        "DEACTIVATE",
                        performedBy,
                        null,
                        auditService.buildChanges("status", "ACTIVE", "INACTIVE")
                );
            } catch (Exception ex) {
                System.err.println("[UserAdminController] Audit error on deactivateOrganizer: " + ex.getMessage());
            }

            return ResponseEntity.ok(toResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID inválido"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activateOrganizer(@PathVariable String id, Principal principal) {
        try {
            User user = userRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            user.setStatus("ACTIVE");
            User saved = userRepository.save(user);

            try {
                Map<String, Object> performedBy = buildPerformedBy(principal);
                auditService.log(
                        "USER", id, null,
                        "ACTIVATE",
                        performedBy,
                        null,
                        auditService.buildChanges("status", "INACTIVE", "ACTIVE")
                );
            } catch (Exception ex) {
                System.err.println("[UserAdminController] Audit error on activateOrganizer: " + ex.getMessage());
            }

            return ResponseEntity.ok(toResponse(saved));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Map<String, Object> buildPerformedBy(Principal principal) {
        if (principal == null) return auditService.buildPerformedBy(null, null, "ADMIN");
        try {
            User admin = userRepository.findByEmail(principal.getName()).orElse(null);
            if (admin != null) {
                return auditService.buildPerformedBy(admin.getId().toString(), admin.getEmail(), admin.getRole());
            }
        } catch (Exception ignored) {}
        return auditService.buildPerformedBy(null, principal.getName(), "ADMIN");
    }

    private UserListResponse toResponse(User u) {
        return new UserListResponse(
                u.getId().toString(),
                u.getEmail(),
                u.getRole(),
                u.getStatus(),
                u.getEmployeeId()
        );
    }
}
