package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.document.UniplanUserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniplanUserDocumentRepository
        extends MongoRepository<UniplanUserDocument, String> {

    Optional<UniplanUserDocument> findByPostgresUserId(String postgresUserId);

    Optional<UniplanUserDocument> findByEmail(String email);

    boolean existsByEmail(String email);
}
