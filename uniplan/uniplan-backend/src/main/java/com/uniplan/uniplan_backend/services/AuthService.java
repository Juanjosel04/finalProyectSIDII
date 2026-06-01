package com.uniplan.uniplan_backend.services;

import com.uniplan.uniplan_backend.config.JwtService;
import com.uniplan.uniplan_backend.dto.AuthResponse;
import com.uniplan.uniplan_backend.dto.LoginRequest;
import com.uniplan.uniplan_backend.dto.RegisterRequest;
import com.uniplan.uniplan_backend.model.relational.uniplan.User;
import com.uniplan.uniplan_backend.repositories.EmployeeRepository;
import com.uniplan.uniplan_backend.repositories.StudentRepository;
import com.uniplan.uniplan_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_ORGANIZER = "ORGANIZER";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.getEmail());
        String role = normalizeRole(request.getRole());
        String studentId = normalizeId(request.getStudentId());
        String employeeId = normalizeId(request.getEmployeeId());

        validateCommonRegisterData(email, request.getPassword(), role);
        validateInstitutionalIdentity(role, email, studentId, employeeId);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .studentId(ROLE_STUDENT.equals(role) ? studentId : null)
                .employeeId((ROLE_ORGANIZER.equals(role) || ROLE_ADMIN.equals(role)) ? employeeId : null)
                .status("ACTIVE")
                .build();

        userRepository.save(user);

        auditService.log(
                "USER", user.getId().toString(), user.getEmail(),
                "REGISTER",
                auditService.buildPerformedBy(user.getId().toString(), user.getEmail(), user.getRole()),
                null,
                Map.of("role", user.getRole(), "status", "ACTIVE")
        );

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of()
        );

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getRole(),
                user.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of()
        );

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getRole(),
                user.getEmail()
        );
    }

    private void  validateCommonRegisterData(String email, String password, String role) {

        if (isBlank(email)) {
            throw new IllegalArgumentException("El correo institucional es obligatorio");
        }

        if (isBlank(password)) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        if (!ROLE_STUDENT.equals(role) && !ROLE_ORGANIZER.equals(role) && !ROLE_ADMIN.equals(role)) {
            throw new IllegalArgumentException("Rol no válido");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con este correo");
        }
    }

    private void validateInstitutionalIdentity(
            String role,
            String email,
            String studentId,
            String employeeId
    ) {

        if (ROLE_STUDENT.equals(role)) {
            validateStudent(email, studentId);
            return;
        }

        validateEmployee(email, employeeId, role);
    }

    private void validateStudent(String email, String studentId) {

        if (isBlank(studentId)) {
            throw new IllegalArgumentException("El ID de estudiante es obligatorio");
        }

        if (userRepository.existsByStudentId(studentId)) {
            throw new IllegalArgumentException("Este estudiante ya tiene una cuenta en UniPlan");
        }

        boolean existsInInstitutionalDb = studentRepository.existsByIdAndEmailIgnoreCase(
                studentId,
                email
        );

        if (!existsInInstitutionalDb) {
            throw new IllegalArgumentException(
                    "El ID de estudiante y el correo no coinciden con la base de datos institucional"
            );
        }
    }

    private void validateEmployee(String email, String employeeId, String role) {

        if (isBlank(employeeId)) {
            throw new IllegalArgumentException("El ID de empleado es obligatorio para " + role);
        }

        if (userRepository.existsByEmployeeId(employeeId)) {
            throw new IllegalArgumentException("Este empleado ya tiene una cuenta en UniPlan");
        }

        boolean existsInInstitutionalDb = employeeRepository.existsByIdAndEmailIgnoreCase(
                employeeId,
                email
        );

        if (!existsInInstitutionalDb) {
            throw new IllegalArgumentException(
                    "El ID de empleado y el correo no coinciden con la base de datos institucional"
            );
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String role) {
        return role == null ? null : role.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeId(String id) {
        return id == null ? null : id.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
