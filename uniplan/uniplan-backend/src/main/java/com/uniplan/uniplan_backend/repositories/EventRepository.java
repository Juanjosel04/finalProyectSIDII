package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.document.embedded.Event;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository
        extends MongoRepository<Event, String> {

    List<Event> findByStatus(String status);

    List<Event> findByType(String type);

    Optional<Event> findByCode(String code);

    List<Event> findByTitleContainingIgnoreCase(String title);

    /*
     * Search inside nested location.venue field
     */
    List<Event> findByLocationVenueContainingIgnoreCase(String venue);

    /*
     * Search by organizer userId (nested field)
     */
    List<Event> findByOrganizerUserId(String organizerUserId);

    /*
     * Filter by modality (VIRTUAL | IN_PERSON | HYBRID)
     */
    List<Event> findByLocationModality(String modality);

    /*
     * Events with available spots (capacity.available > 0)
     */
    @Query("{ 'capacity.available': { $gt: 0 }, 'status': 'ACTIVE' }")
    List<Event> findActiveEventsWithAvailableSpots();
}