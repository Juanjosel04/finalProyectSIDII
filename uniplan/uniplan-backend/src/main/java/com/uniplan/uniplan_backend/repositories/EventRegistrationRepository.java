package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.document.EventRegistrationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository
        extends MongoRepository<EventRegistrationDocument, String> {

    List<EventRegistrationDocument> findByEventId(String eventId);

    // Nested field student.userId — @Query explícito para evitar ambigüedad
    @Query("{ 'student.userId': ?0 }")
    List<EventRegistrationDocument> findByStudentUserId(String userId);

    List<EventRegistrationDocument> findByEventIdAndStatus(String eventId, String status);

    @Query("{ 'eventId': ?0, 'student.userId': ?1 }")
    Optional<EventRegistrationDocument> findByEventIdAndStudentUserId(String eventId, String userId);

    @Query(value = "{ 'eventId': ?0, 'student.userId': ?1 }", exists = true)
    boolean existsByEventIdAndStudentUserId(String eventId, String userId);

    @Query(value = "{ 'eventId': ?0, 'student.userId': ?1, 'status': { $in: ['REGISTERED', 'WAITLIST'] } }", exists = true)
    boolean existsActiveByEventIdAndStudentUserId(String eventId, String userId);

    @Query("{ 'student.userId': ?0, 'status': { $in: ['REGISTERED', 'WAITLIST'] } }")
    List<EventRegistrationDocument> findActiveByStudentUserId(String userId);

    long countByEventIdAndStatus(String eventId, String status);
}
