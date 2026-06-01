package com.uniplan.uniplan_backend.controllers;

import com.uniplan.uniplan_backend.dto.EventParticipationReport;
import com.uniplan.uniplan_backend.dto.OrganizerActivityReport;
import com.uniplan.uniplan_backend.model.document.AuditLog;
import com.uniplan.uniplan_backend.model.document.EventRegistrationDocument;
import com.uniplan.uniplan_backend.model.document.embedded.Event;
import com.uniplan.uniplan_backend.model.relational.uniplan.EventStatistic;
import com.uniplan.uniplan_backend.model.relational.uniplan.User;
import com.uniplan.uniplan_backend.repositories.EventRegistrationRepository;
import com.uniplan.uniplan_backend.repositories.UserRepository;
import com.uniplan.uniplan_backend.services.EventStatisticService;
import com.uniplan.uniplan_backend.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@CrossOrigin
public class ReportController {

    private final ReportService              reportService;
    private final EventStatisticService      statisticService;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository             userRepository;

    /* ================================================================
     * EXISTING REPORTS (from ReportService)
     * ================================================================ */

    @GetMapping("/events")
    public ResponseEntity<List<EventParticipationReport>> eventReport() {
        return ResponseEntity.ok(reportService.getEventParticipationReport());
    }

    @GetMapping("/organizers")
    public ResponseEntity<List<OrganizerActivityReport>> organizerReport() {
        return ResponseEntity.ok(reportService.getOrganizerActivityReport());
    }

    @GetMapping("/audit")
    public ResponseEntity<List<AuditLog>> auditReport() {
        return ResponseEntity.ok(reportService.getRecentAuditLogs());
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(reportService.getPlatformSummary());
    }

    /* ================================================================
     * CSV EXPORTS
     * ================================================================ */

    /**
     * Exporta estadísticas de todos los eventos (PostgreSQL event_statistics).
     */
    @GetMapping("/events/export")
    public ResponseEntity<byte[]> exportEventsCsv() {
        byte[] csv = statisticService.exportEventStatsCsv();
        return csvResponse(csv, "eventos-estadisticas.csv");
    }

    /**
     * Exporta lista de inscritos/asistentes de un evento específico (MongoDB).
     */
    @GetMapping("/attendance/export")
    public ResponseEntity<byte[]> exportAttendanceCsv(@RequestParam String eventId) {
        byte[] csv = statisticService.exportEventAttendanceCsv(eventId);
        return csvResponse(csv, "asistencia-evento.csv");
    }

    /**
     * Exporta actividad de organizadores.
     */
    @GetMapping("/organizers/export")
    public ResponseEntity<byte[]> exportOrganizersCsv() {
        List<OrganizerActivityReport> data = reportService.getOrganizerActivityReport();
        List<Map<String, Object>> rows = data.stream().map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("firstName",      o.getFirstName());
            m.put("lastName",       o.getLastName());
            m.put("email",          o.getEmail());
            m.put("employeeId",     o.getEmployeeId());
            m.put("eventsCount",    o.getEventsCount());
            m.put("totalRegistered",o.getTotalRegistered());
            m.put("totalAttended",  o.getTotalAttended());
            m.put("totalCancelled", o.getTotalCancelled());
            return m;
        }).collect(Collectors.toList());
        byte[] csv = statisticService.exportOrganizerActivityCsv(rows);
        return csvResponse(csv, "actividad-organizadores.csv");
    }

    /* ================================================================
     * INNOVATIVE REPORT 1 — Student Personal Participation
     * Accessible only to STUDENT role (security configured separately)
     * ================================================================ */

    @GetMapping("/my-participation")
    public ResponseEntity<Map<String, Object>> myParticipation(Principal principal) {

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        List<EventRegistrationDocument> regs =
                registrationRepository.findByStudentUserId(user.getId().toString());

        long totalRegistered = regs.stream().filter(r -> "REGISTERED".equals(r.getStatus())).count();
        long totalAttended   = regs.stream().filter(r -> "ATTENDED".equals(r.getStatus())).count();
        long totalCancelled  = regs.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count();
        long totalWaitlist   = regs.stream().filter(r -> "WAITLIST".equals(r.getStatus())).count();

        // Participation score: attended*3 + registered*1 - cancelled*0.5
        double score = totalAttended * 3.0 + totalRegistered * 1.0 - totalCancelled * 0.5;

        // Get event info for each registration
        List<String> eventIds = regs.stream()
                .map(EventRegistrationDocument::getEventId).distinct().collect(Collectors.toList());

        Map<String, Event> eventsMap = new HashMap<>();
        for (var id : eventIds) {
            try {
                statisticService.syncEventAsync(id); // ensure stats are fresh
            } catch (Exception ignored) {}
        }

        // Use EventStatistic (PostgreSQL) to enrich with event titles/types
        // Group registrations by event type via stats
        Map<String, Long> byType = new LinkedHashMap<>();

        // Build enriched registration list (simplified with what we have)
        List<Map<String, Object>> registrationList = regs.stream()
                .sorted(Comparator.comparing(r -> r.getRegisteredAt() == null
                        ? java.time.LocalDateTime.MIN : r.getRegisteredAt(),
                        Comparator.reverseOrder()))
                .limit(20)
                .map(r -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id",           r.getId());
                    item.put("eventId",      r.getEventId());
                    item.put("eventCode",    r.getEventCode());
                    item.put("status",       r.getStatus());
                    item.put("registeredAt", r.getRegisteredAt() != null ? r.getRegisteredAt().toString() : null);
                    item.put("attendedAt",   r.getAttendedAt()   != null ? r.getAttendedAt().toString()   : null);
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("email",            user.getEmail());
        result.put("totalRegistered",  totalRegistered);
        result.put("totalAttended",    totalAttended);
        result.put("totalCancelled",   totalCancelled);
        result.put("totalWaitlist",    totalWaitlist);
        result.put("participationScore", Math.round(score * 10.0) / 10.0);
        result.put("recentRegistrations", registrationList);

        return ResponseEntity.ok(result);
    }

    /* ================================================================
     * INNOVATIVE REPORT 2 — Event Demand Analytics (PostgreSQL stats)
     * ================================================================ */

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> eventAnalytics() {

        Map<String, Object> result = new LinkedHashMap<>();

        // By event type (from PostgreSQL aggregated stats)
        result.put("byType",    statisticService.getEventTypeAnalytics());

        // Top 5 events by registrations
        result.put("topEvents", statisticService.getTopEvents().stream()
                .map(this::statToMap).collect(Collectors.toList()));

        // Alerts: 0 registrations, low attendance
        Map<String, Object> alerts = statisticService.getAlerts();
        result.put("noRegistrations", ((List<?>) alerts.get("noRegistrations")).stream()
                .map(o -> statToMap((EventStatistic) o)).collect(Collectors.toList()));
        result.put("lowAttendance", ((List<?>) alerts.get("lowAttendance")).stream()
                .map(o -> statToMap((EventStatistic) o)).collect(Collectors.toList()));

        return ResponseEntity.ok(result);
    }

    /* ================================================================
     * MANUAL SYNC TRIGGER (admin utility)
     * ================================================================ */

    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> triggerSync() {
        statisticService.syncAll();
        return ResponseEntity.ok(Map.of("message", "Sincronización completada"));
    }

    /* ================================================================
     * HELPERS
     * ================================================================ */

    private ResponseEntity<byte[]> csvResponse(byte[] csv, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    private Map<String, Object> statToMap(EventStatistic s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("eventId",             s.getEventId());
        m.put("eventCode",           s.getEventCode());
        m.put("eventTitle",          s.getEventTitle());
        m.put("eventType",           s.getEventType());
        m.put("eventStatus",         s.getEventStatus());
        m.put("totalCapacity",       s.getTotalCapacity());
        m.put("registered",          s.getRegistered());
        m.put("attended",            s.getAttended());
        m.put("cancelled",           s.getCancelled());
        m.put("waitlist",            s.getWaitlist());
        m.put("occupancyPercentage", s.getOccupancyPercentage());
        m.put("attendanceRate",      s.getAttendanceRate());
        m.put("organizerEmail",      s.getOrganizerEmail());
        m.put("lastSyncedAt",        s.getLastSyncedAt() != null ? s.getLastSyncedAt().toString() : null);
        return m;
    }
}
