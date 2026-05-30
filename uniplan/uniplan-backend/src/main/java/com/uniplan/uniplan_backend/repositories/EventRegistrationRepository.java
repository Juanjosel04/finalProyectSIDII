package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.document.EventRegistrationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository
        extends MongoRepository<EventRegistrationDocument, String> {

    List<EventRegistrationDocument> findByEventId(String eventId);

    List<EventRegistrationDocument> findByStudentUserId(String userId);

    List<EventRegistrationDocument> findByEventIdAndStatus(String eventId, String status);

    Optional<EventRegistrationDocument> findByEventIdAndStudentUserId(String eventId, String userId);

    boolean existsByEventIdAndStudentUserId(String eventId, String userId);

    long countByEventIdAndStatus(String eventId, String status);
}
