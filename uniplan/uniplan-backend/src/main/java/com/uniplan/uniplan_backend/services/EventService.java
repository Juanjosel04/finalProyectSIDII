package com.uniplan.uniplan_backend.services;

import com.uniplan.uniplan_backend.dto.CreateEventRequest;
import com.uniplan.uniplan_backend.dto.EventListResponse;
import com.uniplan.uniplan_backend.dto.EventResponse;
import com.uniplan.uniplan_backend.model.document.embedded.Event;
import com.uniplan.uniplan_backend.model.document.embedded.EventCapacity;
import com.uniplan.uniplan_backend.model.document.embedded.EventOrganizer;
import com.uniplan.uniplan_backend.model.relational.uniplan.User;
import com.uniplan.uniplan_backend.repositories.EventRepository;
import com.uniplan.uniplan_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository         eventRepository;
    private final UserRepository          userRepository;
    private final AuditService            auditService;
    private final EventStatisticService   statisticService;

    /*
     * =========================================================
     * CREATE EVENT
     * =========================================================
     */

    public EventResponse createEvent(CreateEventRequest request, String organizerEmail) {

        // Inicializar cupos
        EventCapacity capacity = request.getCapacity();
        if (capacity != null && capacity.getAvailable() == null) {
            capacity.setAvailable(capacity.getTotal());
            capacity.setRegistered(0);
            capacity.setWaitlist(0);
        }

        // Construir organizer desde el usuario autenticado (JWT)
        EventOrganizer organizer = buildOrganizer(request, organizerEmail);

        LocalDateTime now = LocalDateTime.now();

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .schedule(request.getSchedule())
                .location(request.getLocation())
                .capacity(capacity)
                .organizer(organizer)
                .details(request.getDetails())
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();

        Event savedEvent = eventRepository.save(event);

        auditService.log(
                "EVENT", savedEvent.getId(), savedEvent.getCode(),
                "CREATE", Map.of("role", "SYSTEM"), null,
                Map.of("title", savedEvent.getTitle(), "status", "ACTIVE")
        );

        statisticService.syncEventAsync(savedEvent.getId());

        return new EventResponse(
                savedEvent.getId(),
                savedEvent.getTitle(),
                savedEvent.getStatus()
        );
    }

    /*
     * =========================================================
     * GET ALL EVENTS
     * =========================================================
     */

    public List<EventListResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    /*
     * =========================================================
     * GET EVENTS BY ORGANIZER (para el panel del organizador)
     * =========================================================
     */

    public List<EventListResponse> getMyEvents(String organizerEmail) {
        User user = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return eventRepository.findByOrganizerUserId(user.getId().toString())
                .stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    /*
     * =========================================================
     * SEARCH EVENTS
     * =========================================================
     * Searches: title, location.venue, type
     */

    public List<EventListResponse> searchEvents(String query) {

        String q = query.toLowerCase();

        return eventRepository.findAll()
                .stream()
                .filter(event ->
                        matchesText(event.getTitle(), q)
                        || (event.getLocation() != null && matchesText(event.getLocation().getVenue(), q))
                        || matchesText(event.getType(), q)
                )
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    /*
     * =========================================================
     * FILTER BY TYPE
     * =========================================================
     */

    public List<EventListResponse> filterByType(String type) {
        return eventRepository.findByType(type)
                .stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    /*
     * =========================================================
     * FILTER BY STATUS
     * =========================================================
     */

    public List<EventListResponse> filterByStatus(String status) {
        return eventRepository.findByStatus(status)
                .stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    /*
     * =========================================================
     * GET EVENT BY ID
     * =========================================================
     */

    public Event getEventById(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));
    }

    /*
     * =========================================================
     * CANCEL EVENT (soft delete)
     * =========================================================
     */

    public EventResponse cancelEvent(String id) {

        Event event = getEventById(id);
        event.setStatus("CANCELLED");
        event.setUpdatedAt(LocalDateTime.now());

        Event updated = eventRepository.save(event);

        auditService.log(
                "EVENT", updated.getId(), updated.getCode(),
                "CANCEL", Map.of("role", "SYSTEM"), null,
                Map.of("status", Map.of("from", "ACTIVE", "to", "CANCELLED"))
        );

        statisticService.syncEventAsync(updated.getId());

        return new EventResponse(updated.getId(), updated.getTitle(), updated.getStatus());
    }

    /*
     * =========================================================
     * UPDATE EVENT
     * =========================================================
     */

    public EventResponse updateEvent(String id, CreateEventRequest request) {

        Event event = getEventById(id);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setSchedule(request.getSchedule());
        event.setLocation(request.getLocation());
        event.setCapacity(request.getCapacity());
        event.setOrganizer(request.getOrganizer());
        event.setDetails(request.getDetails());
        event.setUpdatedAt(LocalDateTime.now());

        Event updated = eventRepository.save(event);

        auditService.log(
                "EVENT", updated.getId(), updated.getCode(),
                "UPDATE", Map.of("role", "SYSTEM"), null,
                Map.of("title", updated.getTitle(), "updatedAt", updated.getUpdatedAt().toString())
        );

        statisticService.syncEventAsync(updated.getId());

        return new EventResponse(updated.getId(), updated.getTitle(), updated.getStatus());
    }

    /*
     * =========================================================
     * PRIVATE HELPERS
     * =========================================================
     */

    private EventListResponse mapToListResponse(Event event) {

        String venue = null;
        String modality = null;
        if (event.getLocation() != null) {
            venue = event.getLocation().getVenue();
            modality = event.getLocation().getModality();
        }

        LocalDateTime startDate = null;
        if (event.getSchedule() != null) {
            startDate = event.getSchedule().getStartDate();
        }

        Integer total = null;
        Integer available = null;
        if (event.getCapacity() != null) {
            total = event.getCapacity().getTotal();
            available = event.getCapacity().getAvailable();
        }

        return new EventListResponse(
                event.getId(),
                event.getCode(),
                event.getTitle(),
                event.getType(),
                event.getStatus(),
                startDate,
                venue,
                modality,
                total,
                available
        );
    }

    private boolean matchesText(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    /*
     * Construye el snapshot del organizador desde el usuario autenticado.
     * Si el request ya trae organizer (edición), lo usa. Si no, lo infiere del JWT.
     */
    private EventOrganizer buildOrganizer(CreateEventRequest request, String email) {

        // Si el request ya trae organizer válido, usarlo
        if (request.getOrganizer() != null && request.getOrganizer().getUserId() != null) {
            return request.getOrganizer();
        }

        // Inferir desde el usuario autenticado
        if (email == null) return null;

        return userRepository.findByEmail(email)
                .map(user -> EventOrganizer.builder()
                        .userId(user.getId().toString())
                        .email(user.getEmail())
                        .organizerType(user.getRole())
                        .build()
                )
                .orElse(null);
    }
}