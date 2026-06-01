package com.uniplan.uniplan_backend.controllers;

import com.uniplan.uniplan_backend.dto.AdminRegisterRequest;
import com.uniplan.uniplan_backend.dto.AttendanceRequest;
import com.uniplan.uniplan_backend.dto.RegisterToEventRequest;
import com.uniplan.uniplan_backend.dto.RegistrationResponse;
import com.uniplan.uniplan_backend.services.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
@CrossOrigin
public class RegistrationController {

    private final RegistrationService registrationService;

    /*
     * =========================================================
     * POST /registrations
     * Inscribe al estudiante autenticado en un evento.
     * Role: STUDENT
     * =========================================================
     */
    @PostMapping
    public ResponseEntity<RegistrationResponse> register(
            @RequestBody RegisterToEventRequest request,
            Principal principal
    ) {
        RegistrationResponse response = registrationService.register(
                request.getEventId(),
                principal.getName()   // email del JWT
        );
        return ResponseEntity.ok(response);
    }

    /*
     * =========================================================
     * DELETE /registrations/{id}
     * Cancela la inscripción del estudiante autenticado.
     * Role: STUDENT
     * =========================================================
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RegistrationResponse> cancel(
            @PathVariable String id,
            Principal principal
    ) {
        RegistrationResponse response = registrationService.cancelRegistration(
                id,
                principal.getName()
        );
        return ResponseEntity.ok(response);
    }

    /*
     * =========================================================
     * GET /registrations/my
     * Devuelve las inscripciones del estudiante autenticado.
     * Role: STUDENT
     * =========================================================
     */
    @GetMapping("/my")
    public ResponseEntity<List<RegistrationResponse>> myRegistrations(
            Principal principal
    ) {
        return ResponseEntity.ok(
                registrationService.getMyRegistrations(principal.getName())
        );
    }

    /*
     * =========================================================
     * GET /registrations/event/{eventId}
     * Lista todos los inscritos de un evento.
     * Role: ADMIN | ORGANIZER
     * =========================================================
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<RegistrationResponse>> byEvent(
            @PathVariable String eventId
    ) {
        return ResponseEntity.ok(
                registrationService.getRegistrationsByEvent(eventId)
        );
    }

    /*
     * =========================================================
     * GET /registrations/event/{eventId}/attended
     * Asistidos de un evento, ordenados más nuevo primero.
     * Role: ADMIN | ORGANIZER
     * =========================================================
     */
    @GetMapping("/event/{eventId}/attended")
    public ResponseEntity<List<RegistrationResponse>> attendedByEvent(
            @PathVariable String eventId
    ) {
        return ResponseEntity.ok(registrationService.getAttendedByEvent(eventId));
    }

    /*
     * =========================================================
     * GET /registrations
     * Todas las inscripciones de la plataforma.
     * Role: ADMIN
     * =========================================================
     */
    @GetMapping
    public ResponseEntity<List<RegistrationResponse>> getAllRegistrations() {
        return ResponseEntity.ok(registrationService.getAllRegistrations());
    }

    /*
     * =========================================================
     * POST /registrations/admin
     * El admin inscribe a un estudiante en un evento por correo.
     * Role: ADMIN
     * =========================================================
     */
    @PostMapping("/admin")
    public ResponseEntity<?> adminRegister(@RequestBody AdminRegisterRequest req, Principal principal) {
        try {
            RegistrationResponse response = registrationService.registerWithCaller(
                    req.getEventId(),
                    req.getStudentEmail(),
                    principal.getName()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /*
     * =========================================================
     * GET /registrations/organizer
     * Todas las inscripciones de los eventos del organizador.
     * Role: ORGANIZER
     * =========================================================
     */
    @GetMapping("/organizer")
    public ResponseEntity<List<RegistrationResponse>> organizerRegistrations(Principal principal) {
        return ResponseEntity.ok(
                registrationService.getAllRegistrationsForOrganizer(principal.getName())
        );
    }

    /*
     * =========================================================
     * POST /registrations/attendance
     * Registra la asistencia de un estudiante a un evento.
     * Role: ADMIN | ORGANIZER
     * =========================================================
     */
    @PostMapping("/attendance")
    public ResponseEntity<?> registerAttendance(
            @RequestBody AttendanceRequest req,
            Principal principal
    ) {
        try {
            return ResponseEntity.ok(
                    registrationService.registerAttendance(
                            req.getEventId(),
                            req.getStudentCode(),
                            principal.getName()
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /*
     * =========================================================
     * POST /registrations/organizer
     * El organizador inscribe a un estudiante en uno de sus eventos.
     * Role: ORGANIZER
     * =========================================================
     */
    @PostMapping("/organizer")
    public ResponseEntity<?> organizerRegister(
            @RequestBody AdminRegisterRequest req,
            Principal principal
    ) {
        try {
            RegistrationResponse response = registrationService.registerByOrganizer(
                    req.getEventId(),
                    req.getStudentEmail(),
                    principal.getName()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
