package com.uniplan.uniplan_backend.services;

import com.uniplan.uniplan_backend.model.document.AuditLog;
import com.uniplan.uniplan_backend.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AuditService — reutilizable por todos los módulos.
 *
 * Uso:
 *   auditService.log("EVENT", eventId, eventCode, "CREATE", performedBy, null, changes);
 *
 * Nunca lanza excepción hacia afuera: los errores de auditoría
 * no deben interrumpir la operación principal.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /*
     * =========================================================
     * LOG — método principal
     * =========================================================
     */

    public void log(
            String entity,
            String entityId,
            String entityCode,
            String action,
            Map<String, Object> performedBy,
            Map<String, Object> target,
            Map<String, Object> changes
    ) {
        log(entity, entityId, entityCode, action, performedBy, target, changes, null);
    }

    public void log(
            String entity,
            String entityId,
            String entityCode,
            String action,
            Map<String, Object> performedBy,
            Map<String, Object> target,
            Map<String, Object> changes,
            Map<String, Object> metadata
    ) {
        try {
            AuditLog log = AuditLog.builder()
                    .entity(entity)
                    .entityId(entityId)
                    .entityCode(entityCode)
                    .action(action)
                    .performedBy(performedBy)
                    .target(target)
                    .changes(changes)
                    .metadata(metadata)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(log);

        } catch (Exception e) {
            // Auditoría no debe interrumpir la operación principal
            System.err.println("[AuditService] Error al guardar log: " + e.getMessage());
        }
    }

    /*
     * =========================================================
     * HELPERS — construyen el performedBy desde datos del JWT
     * =========================================================
     */

    public Map<String, Object> buildPerformedBy(String userId, String email, String role) {
        return Map.of(
                "userId", userId != null ? userId : "unknown",
                "email",  email  != null ? email  : "unknown",
                "role",   role   != null ? role   : "unknown"
        );
    }

    public Map<String, Object> buildChanges(String field, Object from, Object to) {
        return Map.of(field, Map.of("from", from != null ? from : "null",
                                    "to",   to   != null ? to   : "null"));
    }
}
