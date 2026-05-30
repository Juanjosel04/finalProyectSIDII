package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.document.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository
        extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByEntityAndEntityIdOrderByCreatedAtDesc(String entity, String entityId);

    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
}
