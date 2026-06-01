package com.uniplan.uniplan_backend.services;

import com.uniplan.uniplan_backend.model.document.EventRegistrationDocument;
import com.uniplan.uniplan_backend.model.document.embedded.Event;
import com.uniplan.uniplan_backend.model.relational.uniplan.EventStatistic;
import com.uniplan.uniplan_backend.repositories.EventRegistrationRepository;
import com.uniplan.uniplan_backend.repositories.EventRepository;
import com.uniplan.uniplan_backend.repositories.EventStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventStatisticService {

    private final EventStatisticRepository statisticRepository;
    private final EventRepository          eventRepository;
    private final EventRegistrationRepository registrationRepository;

    /* ================================================================
     * SYNC — Entry points
     * ================================================================ */

    /**
     * Called after every registration mutation (non-blocking).
     */
    @Async
    public void syncEventAsync(String eventId) {
        try { syncEvent(eventId); }
        catch (Exception e) {
            System.err.println("[EventStatisticService] Async sync error " + eventId + ": " + e.getMessage());
        }
    }

    /**
     * Full sync at startup + every hour.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedRate = 3_600_000)
    public void syncAll() {
        try {
            eventRepository.findAll().forEach(event -> {
                try { syncEvent(event.getId()); }
                catch (Exception e) {
                    System.err.println("[EventStatisticService] syncAll error: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[EventStatisticService] syncAll failed: " + e.getMessage());
        }
    }

    /* ================================================================
     * SYNC — Core
     * ================================================================ */

    @Transactional("transactionManager")
    public void syncEvent(String eventId) {

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return;

        List<EventRegistrationDocument> regs = registrationRepository.findByEventId(eventId);

        int registered = count(regs, "REGISTERED");
        int attended   = count(regs, "ATTENDED");
        int cancelled  = count(regs, "CANCELLED");
        int waitlist   = count(regs, "WAITLIST");

        int total = (event.getCapacity() != null && event.getCapacity().getTotal() != null)
                ? event.getCapacity().getTotal() : 0;

        double occupancy = total > 0
                ? round1((registered + attended) / (double) total * 100.0)
                : 0.0;
        double attendanceRate = (registered + attended) > 0
                ? round1(attended / (double) (registered + attended) * 100.0)
                : 0.0;

        EventStatistic stat = statisticRepository.findById(eventId)
                .orElse(EventStatistic.builder().eventId(eventId).build());

        stat.setEventCode(event.getCode());
        stat.setEventTitle(event.getTitle());
        stat.setEventType(event.getType());
        stat.setEventStatus(event.getStatus());
        stat.setTotalCapacity(total > 0 ? total : null);
        stat.setRegistered(registered);
        stat.setAttended(attended);
        stat.setCancelled(cancelled);
        stat.setWaitlist(waitlist);
        stat.setOccupancyPercentage(occupancy);
        stat.setAttendanceRate(attendanceRate);
        stat.setOrganizerEmail(event.getOrganizer() != null ? event.getOrganizer().getEmail() : null);
        stat.setEventStartDate(event.getSchedule() != null ? event.getSchedule().getStartDate() : null);
        stat.setLastSyncedAt(LocalDateTime.now());

        statisticRepository.save(stat);
    }

    /* ================================================================
     * CSV EXPORTS
     * ================================================================ */

    public byte[] exportEventStatsCsv() {
        List<EventStatistic> stats = statisticRepository.findAll();
        StringBuilder sb = new StringBuilder("﻿"); // BOM for Excel
        sb.append("ID;Código;Evento;Tipo;Estado;Capacidad;Inscritos;Asistidos;Cancelados;En Espera;Ocupación %;Tasa Asistencia %;Fecha Inicio;Organizador;Última Sincronización\n");
        stats.forEach(s -> {
            sb.append(String.join(";",
                    q(s.getEventId()),
                    q(s.getEventCode()),
                    q(s.getEventTitle()), q(s.getEventType()), q(s.getEventStatus()),
                    s.getTotalCapacity() != null ? s.getTotalCapacity().toString() : "",
                    str(s.getRegistered()), str(s.getAttended()), str(s.getCancelled()), str(s.getWaitlist()),
                    dbl(s.getOccupancyPercentage()), dbl(s.getAttendanceRate()),
                    s.getEventStartDate() != null ? s.getEventStartDate().toLocalDate().toString() : "",
                    q(s.getOrganizerEmail()),
                    s.getLastSyncedAt() != null ? s.getLastSyncedAt().toLocalDate().toString() : ""
            )).append("\n");
        });
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportEventAttendanceCsv(String eventId) {
        List<EventRegistrationDocument> regs = registrationRepository.findByEventId(eventId);

        StringBuilder sb = new StringBuilder("﻿");
        sb.append("ID Inscripción;Nombre Completo;Código Estudiante;Correo;Estado;Fecha Inscripción;Fecha Asistencia\n");
        regs.forEach(r -> {
            var s = r.getStudent();
            String name  = s != null ? q(trim(s.getFirstName()) + " " + trim(s.getLastName())) : "";
            String code  = s != null ? q(s.getStudentId()) : "";
            String email = s != null ? q(s.getEmail())     : "";
            sb.append(String.join(";",
                    q(r.getId()), name, code, email, q(r.getStatus()),
                    r.getRegisteredAt() != null ? r.getRegisteredAt().toLocalDate().toString() : "",
                    r.getAttendedAt()   != null ? r.getAttendedAt().toLocalDate().toString()   : ""
            )).append("\n");
        });
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportOrganizerActivityCsv(List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("Nombre;Correo;ID Empleado;Eventos;Inscritos;Asistidos;Cancelados\n");
        data.forEach(row -> sb.append(String.join(";",
                q(str(row.get("firstName")) + " " + str(row.get("lastName"))),
                q(str(row.get("email"))),
                q(str(row.get("employeeId"))),
                str(row.get("eventsCount")),
                str(row.get("totalRegistered")),
                str(row.get("totalAttended")),
                str(row.get("totalCancelled"))
        )).append("\n"));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /* ================================================================
     * ANALYTICS — Powered by event_statistics (PostgreSQL)
     * ================================================================ */

    /**
     * Aggregated stats grouped by event type.
     * Cross-DB: PostgreSQL event_statistics (aggregated from MongoDB).
     */
    public List<Map<String, Object>> getEventTypeAnalytics() {
        List<EventStatistic> all = statisticRepository.findAll();

        Map<String, List<EventStatistic>> byType = all.stream()
                .filter(s -> s.getEventType() != null)
                .collect(Collectors.groupingBy(EventStatistic::getEventType));

        List<Map<String, Object>> result = new ArrayList<>();
        byType.forEach((type, list) -> {
            long totalEvents     = list.size();
            long totalRegistered = list.stream().mapToLong(s -> orZero(s.getRegistered())).sum();
            long totalAttended   = list.stream().mapToLong(s -> orZero(s.getAttended())).sum();
            long totalCancelled  = list.stream().mapToLong(s -> orZero(s.getCancelled())).sum();
            double avgOccupancy  = list.stream()
                    .filter(s -> s.getOccupancyPercentage() != null)
                    .mapToDouble(EventStatistic::getOccupancyPercentage)
                    .average().orElse(0);
            double avgAttendRate = list.stream()
                    .filter(s -> s.getAttendanceRate() != null)
                    .mapToDouble(EventStatistic::getAttendanceRate)
                    .average().orElse(0);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("type",              type);
            row.put("totalEvents",       totalEvents);
            row.put("totalRegistered",   totalRegistered);
            row.put("totalAttended",     totalAttended);
            row.put("totalCancelled",    totalCancelled);
            row.put("avgOccupancy",      round1(avgOccupancy));
            row.put("avgAttendanceRate", round1(avgAttendRate));
            result.add(row);
        });

        result.sort((a, b) -> Long.compare(
                (Long) b.get("totalRegistered"), (Long) a.get("totalRegistered")));
        return result;
    }

    /**
     * Top 5 events by registration count (from PostgreSQL stats).
     */
    public List<EventStatistic> getTopEvents() {
        return statisticRepository.findAllByOrderByRegisteredDesc()
                .stream().limit(5).collect(Collectors.toList());
    }

    /**
     * Events with 0 registrations (active) or low attendance rate.
     */
    public Map<String, Object> getAlerts() {
        Map<String, Object> alerts = new LinkedHashMap<>();
        alerts.put("noRegistrations", statisticRepository.findActiveEventsWithNoRegistrations());
        alerts.put("lowAttendance",   statisticRepository.findLowAttendanceEvents());
        return alerts;
    }

    /* ================================================================
     * HELPERS
     * ================================================================ */

    private int count(List<EventRegistrationDocument> regs, String status) {
        return (int) regs.stream().filter(r -> status.equals(r.getStatus())).count();
    }

    private double round1(double val) {
        return Math.round(val * 10.0) / 10.0;
    }

    private int orZero(Integer i) { return i == null ? 0 : i; }

    private String q(String val) {
        if (val == null) return "";
        val = val.trim();
        // Escape values that contain the delimiter (;), quotes, or newlines
        if (val.contains(";") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private String str(Object val) { return val == null ? "" : val.toString(); }
    private String dbl(Double val)  { return val == null ? "0" : String.format("%.1f", val); }
    private String trim(String val) { return val == null ? "" : val.trim(); }
}
