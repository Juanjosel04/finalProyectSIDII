package com.uniplan.uniplan_backend.services;

import com.uniplan.uniplan_backend.dto.EventParticipationReport;
import com.uniplan.uniplan_backend.dto.OrganizerActivityReport;
import com.uniplan.uniplan_backend.model.document.AuditLog;
import com.uniplan.uniplan_backend.model.document.EventRegistrationDocument;
import com.uniplan.uniplan_backend.model.document.embedded.Event;
import com.uniplan.uniplan_backend.model.relational.university.Employee;
import com.uniplan.uniplan_backend.model.relational.uniplan.User;
import com.uniplan.uniplan_backend.repositories.AuditLogRepository;
import com.uniplan.uniplan_backend.repositories.EmployeeRepository;
import com.uniplan.uniplan_backend.repositories.EventRegistrationRepository;
import com.uniplan.uniplan_backend.repositories.EventRepository;
import com.uniplan.uniplan_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Event participation report — MongoDB only.
     * For each event, counts registrations by status and computes attendance rate.
     */
    public List<EventParticipationReport> getEventParticipationReport() {

        List<Event> events = eventRepository.findAll();
        List<EventRegistrationDocument> allRegs = registrationRepository.findAll();

        // Group registrations by eventId, then by status
        Map<String, Map<String, Long>> regsByEventAndStatus = allRegs.stream()
                .collect(Collectors.groupingBy(
                        EventRegistrationDocument::getEventId,
                        Collectors.groupingBy(
                                EventRegistrationDocument::getStatus,
                                Collectors.counting()
                        )
                ));

        List<EventParticipationReport> result = new ArrayList<>();

        for (Event event : events) {
            Map<String, Long> statusCounts = regsByEventAndStatus.getOrDefault(event.getId(), Map.of());

            int registered = statusCounts.getOrDefault("REGISTERED", 0L).intValue();
            int attended   = statusCounts.getOrDefault("ATTENDED",   0L).intValue();
            int waitlist   = statusCounts.getOrDefault("WAITLIST",   0L).intValue();
            int cancelled  = statusCounts.getOrDefault("CANCELLED",  0L).intValue();

            double attendanceRate = 0.0;
            int denominator = registered + attended;
            if (denominator > 0) {
                attendanceRate = Math.round((attended / (double) denominator) * 1000.0) / 10.0;
            }

            Integer totalCapacity = null;
            if (event.getCapacity() != null) {
                totalCapacity = event.getCapacity().getTotal();
            }

            String organizerEmail = null;
            if (event.getOrganizer() != null) {
                organizerEmail = event.getOrganizer().getEmail();
            }

            String startDate = null;
            if (event.getSchedule() != null && event.getSchedule().getStartDate() != null) {
                startDate = event.getSchedule().getStartDate().toString();
            }

            result.add(EventParticipationReport.builder()
                    .eventId(event.getId())
                    .eventCode(event.getCode())
                    .eventTitle(event.getTitle())
                    .eventType(event.getType())
                    .eventStatus(event.getStatus())
                    .organizerEmail(organizerEmail)
                    .startDate(startDate)
                    .totalCapacity(totalCapacity)
                    .registered(registered)
                    .attended(attended)
                    .waitlist(waitlist)
                    .cancelled(cancelled)
                    .attendanceRate(attendanceRate)
                    .build());
        }

        result.sort(Comparator.comparing(e -> e.getEventTitle() != null ? e.getEventTitle() : ""));

        return result;
    }

    /**
     * Organizer activity report — Cross-DB (PostgreSQL users + MongoDB events/registrations).
     */
    public List<OrganizerActivityReport> getOrganizerActivityReport() {

        List<User> organizers = userRepository.findByRole("ORGANIZER");
        List<Event> allEvents = eventRepository.findAll();
        List<EventRegistrationDocument> allRegs = registrationRepository.findAll();

        // Group events by organizer userId
        Map<String, List<Event>> eventsByOrganizer = new HashMap<>();
        for (Event event : allEvents) {
            if (event.getOrganizer() == null || event.getOrganizer().getUserId() == null) {
                continue;
            }
            String organizerUserId = event.getOrganizer().getUserId();
            eventsByOrganizer.computeIfAbsent(organizerUserId, k -> new ArrayList<>()).add(event);
        }

        // Group registrations by eventId, then by status
        Map<String, Map<String, Long>> regsByEventAndStatus = allRegs.stream()
                .collect(Collectors.groupingBy(
                        EventRegistrationDocument::getEventId,
                        Collectors.groupingBy(
                                EventRegistrationDocument::getStatus,
                                Collectors.counting()
                        )
                ));

        List<OrganizerActivityReport> result = new ArrayList<>();

        for (User organizer : organizers) {
            String userId = organizer.getId().toString();
            List<Event> organizerEvents = eventsByOrganizer.getOrDefault(userId, List.of());

            long totalRegistered = 0;
            long totalAttended   = 0;
            long totalCancelled  = 0;

            for (Event event : organizerEvents) {
                Map<String, Long> statusCounts = regsByEventAndStatus.getOrDefault(event.getId(), Map.of());
                totalRegistered += statusCounts.getOrDefault("REGISTERED", 0L);
                totalAttended   += statusCounts.getOrDefault("ATTENDED",   0L);
                totalCancelled  += statusCounts.getOrDefault("CANCELLED",  0L);
            }

            String firstName = null;
            String lastName  = null;
            String employeeId = organizer.getEmployeeId();

            if (employeeId != null) {
                Optional<Employee> emp = employeeRepository.findById(employeeId);
                if (emp.isPresent()) {
                    firstName = emp.get().getFirstName();
                    lastName  = emp.get().getLastName();
                }
            }

            result.add(OrganizerActivityReport.builder()
                    .userId(userId)
                    .email(organizer.getEmail())
                    .firstName(firstName)
                    .lastName(lastName)
                    .employeeId(employeeId)
                    .eventsCount(organizerEvents.size())
                    .totalRegistered(totalRegistered)
                    .totalAttended(totalAttended)
                    .totalCancelled(totalCancelled)
                    .build());
        }

        result.sort(Comparator.comparingLong(OrganizerActivityReport::getEventsCount).reversed());

        return result;
    }

    /**
     * Returns the 100 most recent audit log entries.
     */
    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }

    /**
     * Platform summary — Cross-DB.
     */
    public Map<String, Object> getPlatformSummary() {

        long totalUsers        = userRepository.count();
        long totalOrganizers   = userRepository.countByRole("ORGANIZER");
        long totalStudents     = userRepository.countByRole("STUDENT");
        long totalEvents       = eventRepository.count();
        long totalRegistrations = registrationRepository.count();
        long totalAuditLogs    = auditLogRepository.count();

        int activeEvents = (int) eventRepository.findAll().stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .count();

        // Group all registrations by status and count each
        Map<String, Long> registrationsByStatus = registrationRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        EventRegistrationDocument::getStatus,
                        Collectors.counting()
                ));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalUsers",          totalUsers);
        summary.put("totalOrganizers",     totalOrganizers);
        summary.put("totalStudents",       totalStudents);
        summary.put("totalEvents",         totalEvents);
        summary.put("activeEvents",        activeEvents);
        summary.put("totalRegistrations",  totalRegistrations);
        summary.put("registrationsByStatus", registrationsByStatus);
        summary.put("totalAuditLogs",      totalAuditLogs);

        return summary;
    }
}
