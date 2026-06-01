package com.uniplan.uniplan_backend.services;

import com.uniplan.uniplan_backend.dto.RegistrationResponse;
import com.uniplan.uniplan_backend.model.document.EventRegistrationDocument;
import com.uniplan.uniplan_backend.model.document.embedded.Event;
import com.uniplan.uniplan_backend.model.document.embedded.StudentSnapshot;
import com.uniplan.uniplan_backend.model.relational.university.Student;
import com.uniplan.uniplan_backend.model.relational.uniplan.User;
import com.uniplan.uniplan_backend.repositories.EnrollmentRepository;
import com.uniplan.uniplan_backend.repositories.EventRegistrationRepository;
import com.uniplan.uniplan_backend.repositories.EventRepository;
import com.uniplan.uniplan_backend.repositories.StudentRepository;
import com.uniplan.uniplan_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventRepository             eventRepository;
    private final UserRepository              userRepository;
    private final StudentRepository           studentRepository;
    private final EnrollmentRepository        enrollmentRepository;
    private final MongoTemplate               mongoTemplate;
    private final AuditService                auditService;

    /*
     * =========================================================
     * REGISTER STUDENT TO EVENT
     * =========================================================
     *
     * Validaciones:
     *  1. El evento existe y está ACTIVE
     *  2. El usuario existe y tiene rol STUDENT
     *  3. El estudiante existe en la BD institucional
     *  4. No está ya inscrito en este evento
     *  5. Hay cupos disponibles (operación atómica)
     *
     * Cupos atómicos:
     *  Usamos findAndModify con $inc y filtro capacity.available > 0
     *  para evitar race conditions. Si el resultado es null,
     *  no había cupos.
     *
     * =========================================================
     */

    public RegistrationResponse register(String eventId, String userEmail) {

        /* ── 1. Buscar usuario en PostgreSQL ── */
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!"STUDENT".equals(user.getRole())) {
            throw new IllegalStateException("Solo los estudiantes pueden inscribirse a eventos");
        }

        /* ── 2. Buscar datos del estudiante en BD institucional ── */
        Student student = studentRepository.findById(user.getStudentId())
                .orElseThrow(() -> new RuntimeException(
                        "Estudiante no encontrado en la base de datos institucional"));

        /* ── 3. Verificar que el evento existe y está activo ── */
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (!"ACTIVE".equals(event.getStatus())) {
            throw new IllegalStateException(
                    "No es posible inscribirse: el evento está " + event.getStatus());
        }

        /* ── 4. Verificar que no está ya inscrito (ignora canceladas) ── */
        boolean alreadyRegistered = registrationRepository
                .existsActiveByEventIdAndStudentUserId(eventId, user.getId().toString());

        if (alreadyRegistered) {
            throw new IllegalStateException("Ya estás inscrito en este evento");
        }

        /* ── 4b. Validaciones específicas por tipo de evento ── */
        validateByEventType(event, user, student);

        /* ── 5. Decremento atómico de cupos ── */
        // Solo decrementa si capacity.available > 0.
        // Si el resultado es null → no había cupos.
        Query capacityQuery = new Query(
                Criteria.where("_id").is(eventId)
                        .and("capacity.available").gt(0)
                        .and("status").is("ACTIVE")
        );
        Update capacityUpdate = new Update()
                .inc("capacity.available", -1)
                .inc("capacity.registered", 1);

        Event updatedEvent = mongoTemplate.findAndModify(
                capacityQuery,
                capacityUpdate,
                FindAndModifyOptions.options().returnNew(true),
                Event.class
        );

        if (updatedEvent == null) {
            // Evento lleno — verificar si hay lista de espera
            Event currentEvent = eventRepository.findById(eventId).orElseThrow();
            boolean waitlistEnabled = currentEvent.getCapacity() != null
                    && Boolean.TRUE.equals(currentEvent.getCapacity().getWaitlistEnabled());

            if (!waitlistEnabled) {
                throw new IllegalStateException("El evento no tiene cupos disponibles");
            }

            // Inscribir en lista de espera (sin decrementar cupos)
            return registerToWaitlist(eventId, event, user, student);
        }

        /* ── 6. Construir snapshot y crear registro ── */
        StudentSnapshot snapshot = buildStudentSnapshot(user, student);

        Map<String, Object> validationSnapshot = buildValidationSnapshot(user, student, event);

        LocalDateTime now = LocalDateTime.now();

        EventRegistrationDocument registration = EventRegistrationDocument.builder()
                .eventId(eventId)
                .eventCode(event.getCode())
                .student(snapshot)
                .status("REGISTERED")
                .validationSnapshot(validationSnapshot)
                .registeredAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        EventRegistrationDocument saved = registrationRepository.save(registration);

        /* ── 7. Auditoría ── */
        auditService.log(
                "REGISTRATION", saved.getId(), eventId,
                "REGISTER",
                auditService.buildPerformedBy(user.getId().toString(), user.getEmail(), user.getRole()),
                Map.of("eventId", eventId, "eventTitle", event.getTitle() != null ? event.getTitle() : ""),
                Map.of("status", "REGISTERED", "studentId", student.getId())
        );

        int availableAfter = updatedEvent.getCapacity() != null
                ? updatedEvent.getCapacity().getAvailable() : 0;

        return RegistrationResponse.builder()
                .id(saved.getId())
                .eventId(saved.getEventId())
                .eventCode(saved.getEventCode())
                .status(saved.getStatus())
                .registeredAt(saved.getRegisteredAt())
                .eventTitle(event.getTitle())
                .availableSpotsAfter(availableAfter)
                .build();
    }

    /*
     * =========================================================
     * GET ALL REGISTRATIONS FOR ORGANIZER'S EVENTS
     * =========================================================
     */

    public List<RegistrationResponse> getAllRegistrationsForOrganizer(String organizerEmail) {

        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("Organizador no encontrado"));

        List<String> eventIds = eventRepository
                .findByOrganizerUserId(organizer.getId().toString())
                .stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        if (eventIds.isEmpty()) return List.of();

        Map<String, String> eventTitles = eventRepository.findAllById(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> e.getTitle() != null ? e.getTitle() : ""
                ));

        return registrationRepository.findByEventIdIn(eventIds)
                .stream()
                .map(r -> RegistrationResponse.builder()
                        .id(r.getId())
                        .eventId(r.getEventId())
                        .eventCode(r.getEventCode())
                        .eventTitle(eventTitles.getOrDefault(r.getEventId(), ""))
                        .status(r.getStatus())
                        .registeredAt(r.getRegisteredAt())
                        .cancelledAt(r.getCancelledAt())
                        .cancellationReason(r.getCancellationReason())
                        .studentId(r.getStudent()        != null ? r.getStudent().getStudentId() : null)
                        .studentFirstName(r.getStudent() != null ? r.getStudent().getFirstName() : null)
                        .studentLastName(r.getStudent()  != null ? r.getStudent().getLastName()  : null)
                        .studentEmail(r.getStudent()     != null ? r.getStudent().getEmail()      : null)
                        .build()
                )
                .collect(Collectors.toList());
    }

    /*
     * =========================================================
     * REGISTER BY ORGANIZER (valida que el evento sea suyo)
     * =========================================================
     */

    public RegistrationResponse registerByOrganizer(
            String eventId, String studentEmail, String organizerEmail) {

        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("Organizador no encontrado"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        boolean ownsEvent = event.getOrganizer() != null &&
                organizer.getId().toString().equals(event.getOrganizer().getUserId());

        if (!ownsEvent) {
            throw new IllegalStateException("No tienes permiso para inscribir estudiantes en este evento");
        }

        return register(eventId, studentEmail);
    }

    /*
     * =========================================================
     * CANCEL REGISTRATION
     * =========================================================
     * Devuelve el cupo al evento (atomic increment).
     * Solo el propio estudiante puede cancelar su inscripción.
     * =========================================================
     */

    public RegistrationResponse cancelRegistration(String registrationId, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        EventRegistrationDocument registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        /* Verificar que es la inscripción del usuario ── */
        if (!registration.getStudent().getUserId().equals(user.getId().toString())) {
            throw new IllegalStateException("No tienes permiso para cancelar esta inscripción");
        }

        if (!"REGISTERED".equals(registration.getStatus()) &&
            !"WAITLIST".equals(registration.getStatus())) {
            throw new IllegalStateException(
                    "No se puede cancelar una inscripción con estado: " + registration.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        String previousStatus = registration.getStatus();

        registration.setStatus("CANCELLED");
        registration.setCancelledAt(now);
        registration.setCancellationReason("Cancelada por el estudiante");
        registration.setUpdatedAt(now);

        registrationRepository.save(registration);

        /* Devolver el cupo solo si estaba REGISTERED (no en WAITLIST) ── */
        if ("REGISTERED".equals(previousStatus)) {
            Query restoreQuery = new Query(Criteria.where("_id").is(registration.getEventId()));
            Update restoreUpdate = new Update()
                    .inc("capacity.available", 1)
                    .inc("capacity.registered", -1);
            mongoTemplate.updateFirst(restoreQuery, restoreUpdate, Event.class);
        }

        /* Auditoría ── */
        auditService.log(
                "REGISTRATION", registrationId, registration.getEventId(),
                "CANCEL_REGISTRATION",
                auditService.buildPerformedBy(user.getId().toString(), user.getEmail(), user.getRole()),
                null,
                auditService.buildChanges("status", previousStatus, "CANCELLED")
        );

        return RegistrationResponse.builder()
                .id(registration.getId())
                .eventId(registration.getEventId())
                .status(registration.getStatus())
                .cancelledAt(registration.getCancelledAt())
                .cancellationReason(registration.getCancellationReason())
                .build();
    }

    /*
     * =========================================================
     * GET MY REGISTRATIONS
     * =========================================================
     */

    public List<RegistrationResponse> getMyRegistrations(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return registrationRepository
                .findByStudentUserId(user.getId().toString())
                .stream()
                .map(r -> RegistrationResponse.builder()
                        .id(r.getId())
                        .eventId(r.getEventId())
                        .eventCode(r.getEventCode())
                        .status(r.getStatus())
                        .registeredAt(r.getRegisteredAt())
                        .cancelledAt(r.getCancelledAt())
                        .cancellationReason(r.getCancellationReason())
                        .build()
                )
                .collect(Collectors.toList());
    }

    /*
     * =========================================================
     * GET ALL REGISTRATIONS (admin — todas las inscripciones)
     * =========================================================
     */

    public List<RegistrationResponse> getAllRegistrations() {

        List<EventRegistrationDocument> all = registrationRepository.findAll();

        // Batch fetch events to enrich with titles
        List<String> eventIds = all.stream()
                .map(EventRegistrationDocument::getEventId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> eventTitles = eventRepository.findAllById(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> e.getTitle() != null ? e.getTitle() : ""
                ));

        return all.stream()
                .map(r -> RegistrationResponse.builder()
                        .id(r.getId())
                        .eventId(r.getEventId())
                        .eventCode(r.getEventCode())
                        .eventTitle(eventTitles.getOrDefault(r.getEventId(), ""))
                        .status(r.getStatus())
                        .registeredAt(r.getRegisteredAt())
                        .cancelledAt(r.getCancelledAt())
                        .cancellationReason(r.getCancellationReason())
                        .studentId(r.getStudent()        != null ? r.getStudent().getStudentId() : null)
                        .studentFirstName(r.getStudent() != null ? r.getStudent().getFirstName() : null)
                        .studentLastName(r.getStudent()  != null ? r.getStudent().getLastName()  : null)
                        .studentEmail(r.getStudent()     != null ? r.getStudent().getEmail()      : null)
                        .build()
                )
                .collect(Collectors.toList());
    }

    /*
     * =========================================================
     * GET REGISTRATIONS BY EVENT (para admin/organizer)
     * =========================================================
     */

    public List<RegistrationResponse> getRegistrationsByEvent(String eventId) {

        return registrationRepository
                .findByEventId(eventId)
                .stream()
                .map(r -> RegistrationResponse.builder()
                        .id(r.getId())
                        .eventId(r.getEventId())
                        .eventCode(r.getEventCode())
                        .status(r.getStatus())
                        .registeredAt(r.getRegisteredAt())
                        .cancelledAt(r.getCancelledAt())
                        .cancellationReason(r.getCancellationReason())
                        .studentId(r.getStudent() != null ? r.getStudent().getStudentId() : null)
                        .studentFirstName(r.getStudent() != null ? r.getStudent().getFirstName() : null)
                        .studentLastName(r.getStudent() != null ? r.getStudent().getLastName() : null)
                        .studentEmail(r.getStudent() != null ? r.getStudent().getEmail() : null)
                        .build()
                )
                .collect(Collectors.toList());
    }

    /*
     * =========================================================
     * PRIVATE HELPERS
     * =========================================================
     */

    private RegistrationResponse registerToWaitlist(
            String eventId, Event event, User user, Student student) {

        StudentSnapshot snapshot = buildStudentSnapshot(user, student);
        Map<String, Object> validationSnapshot = buildValidationSnapshot(user, student, event);
        LocalDateTime now = LocalDateTime.now();

        EventRegistrationDocument registration = EventRegistrationDocument.builder()
                .eventId(eventId)
                .eventCode(event.getCode())
                .student(snapshot)
                .status("WAITLIST")
                .validationSnapshot(validationSnapshot)
                .registeredAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        EventRegistrationDocument saved = registrationRepository.save(registration);

        auditService.log(
                "REGISTRATION", saved.getId(), eventId,
                "WAITLIST",
                auditService.buildPerformedBy(user.getId().toString(), user.getEmail(), user.getRole()),
                Map.of("eventId", eventId),
                Map.of("status", "WAITLIST")
        );

        return RegistrationResponse.builder()
                .id(saved.getId())
                .eventId(saved.getEventId())
                .status("WAITLIST")
                .registeredAt(saved.getRegisteredAt())
                .eventTitle(event.getTitle())
                .availableSpotsAfter(0)
                .build();
    }

    private StudentSnapshot buildStudentSnapshot(User user, Student student) {
        return StudentSnapshot.builder()
                .userId(user.getId().toString())
                .studentId(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .build();
    }

    private Map<String, Object> buildValidationSnapshot(User user, Student student, Event event) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("studentId",    student.getId());
        snapshot.put("email",        student.getEmail());
        snapshot.put("eventId",      event.getId());
        snapshot.put("eventTitle",   event.getTitle());
        snapshot.put("eventStatus",  event.getStatus());
        snapshot.put("eventType",    event.getType());
        snapshot.put("validatedAt",  LocalDateTime.now().toString());

        if (event.getCapacity() != null) {
            snapshot.put("capacityAtRegistration", Map.of(
                    "total",     event.getCapacity().getTotal(),
                    "available", event.getCapacity().getAvailable(),
                    "registered", event.getCapacity().getRegistered()
            ));
        }
        return snapshot;
    }

    /*
     * =========================================================
     * TYPE-SPECIFIC VALIDATIONS
     * =========================================================
     */

    private void validateByEventType(Event event, User user, Student student) {
        if (event.getType() == null) return;

        switch (event.getType().toUpperCase()) {
            case "WORKSHOP" -> validateWorkshop(event, student);
            case "SPORT"    -> validateSport(event, user);
            case "VOLUNTEER"-> validateVolunteer(event);
            // ACADEMIC, CULTURAL, OTHER: no extra validation
        }
    }

    private void validateWorkshop(Event event, Student student) {
        if (event.getDetails() == null) return;

        Object prereqObj = event.getDetails().get("prerequisiteSubjectCode");
        if (prereqObj != null) {
            String prereqCode = prereqObj.toString();
            boolean hasPrereq = enrollmentRepository
                    .existsByStudentIdAndSubjectCode(student.getId(), prereqCode);
            if (!hasPrereq) {
                throw new IllegalStateException(
                        "Debes haber cursado la materia '" + prereqCode +
                        "' para inscribirte en este taller");
            }
        }

        Object minSemObj = event.getDetails().get("minimumSemester");
        if (minSemObj != null) {
            int minSemester = Integer.parseInt(minSemObj.toString());
            long studentSemester = enrollmentRepository
                    .countDistinctSemestersByStudentId(student.getId());
            if (studentSemester < minSemester) {
                throw new IllegalStateException(
                        "Este taller requiere estar en semestre " + minSemester +
                        " o superior. Tu semestre actual es: " + studentSemester);
            }
        }
    }

    private void validateSport(Event event, User user) {
        if (event.getSchedule() == null
                || event.getSchedule().getStartDate() == null
                || event.getSchedule().getEndDate() == null) {
            return;
        }

        LocalDateTime newStart = event.getSchedule().getStartDate();
        LocalDateTime newEnd   = event.getSchedule().getEndDate();

        List<EventRegistrationDocument> activeRegs = registrationRepository
                .findActiveByStudentUserId(user.getId().toString());

        List<String> otherEventIds = activeRegs.stream()
                .map(EventRegistrationDocument::getEventId)
                .filter(id -> !id.equals(event.getId()))
                .collect(Collectors.toList());

        if (otherEventIds.isEmpty()) return;

        List<Event> otherEvents = eventRepository.findAllById(otherEventIds);

        for (Event other : otherEvents) {
            if (!"SPORT".equals(other.getType())) continue;
            if (other.getSchedule() == null) continue;

            LocalDateTime otherStart = other.getSchedule().getStartDate();
            LocalDateTime otherEnd   = other.getSchedule().getEndDate();

            if (otherStart == null || otherEnd == null) continue;

            // Overlap: A.start < B.end AND B.start < A.end
            if (newStart.isBefore(otherEnd) && otherStart.isBefore(newEnd)) {
                throw new IllegalStateException(
                        "Ya estás inscrito en el torneo '" + other.getTitle() +
                        "' que tiene un horario que se traslapa con este evento");
            }
        }
    }

    private void validateVolunteer(Event event) {
        // minimumHours es opcional; si está presente queda guardado en details
    }
}
