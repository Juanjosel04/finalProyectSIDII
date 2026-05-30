package com.uniplan.uniplan_backend.controllers;

import com.uniplan.uniplan_backend.dto.RegisterToEventRequest;
import com.uniplan.uniplan_backend.dto.RegistrationResponse;
import com.uniplan.uniplan_backend.services.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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
}
