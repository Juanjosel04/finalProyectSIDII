package com.uniplan.uniplan_backend.services;

import com.uniplan.uniplan_backend.dto.CreateEventRequest;

import com.uniplan.uniplan_backend.dto.EventListResponse;

import com.uniplan.uniplan_backend.dto.EventResponse;

import com.uniplan.uniplan_backend.model.document.embedded.Event;

import com.uniplan.uniplan_backend.repositories.EventRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.List;

import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;



    /*
     * =========================================================
     * CREATE EVENT
     * =========================================================
     */

    public EventResponse createEvent(
            CreateEventRequest request
    ) {

        Event event = Event.builder()

                .title(
                        request.getTitle()
                )

                .description(
                        request.getDescription()
                )

                .type(
                        request.getType()
                )

                .location(
                        request.getLocation()
                )

                .capacity(
                        request.getCapacity()
                )

                .startDate(
                        request.getStartDate()
                )

                .endDate(
                        request.getEndDate()
                )

                .tags(
                        request.getTags()
                )

                .metadata(
                        request.getMetadata()
                )

                .status("ACTIVE")

                .createdAt(
                        LocalDateTime.now()
                )

                .build();



        Event savedEvent =
                eventRepository.save(event);



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

        return eventRepository

                .findAll()

                .stream()

                .map(this::mapToListResponse)

                .collect(Collectors.toList());
    }



    /*
     * =========================================================
     * SEARCH EVENTS
     * =========================================================
     *
     * Search by:
     * - title
     * - location
     * - type
     * - tags
     *
     */

    public List<EventListResponse> searchEvents(
            String query
    ) {

        String normalizedQuery =
                query.toLowerCase();



        List<Event> allEvents =
                eventRepository.findAll();



        List<Event> filteredEvents =
                allEvents.stream()

                        .filter(event ->

                                /*
                                 * TITLE
                                 */

                                event.getTitle() != null

                                        &&

                                        event.getTitle()
                                                .toLowerCase()
                                                .contains(
                                                        normalizedQuery
                                                )



                                        ||

                                        /*
                                         * LOCATION
                                         */

                                        event.getLocation() != null

                                                &&

                                                event.getLocation()
                                                        .toLowerCase()
                                                        .contains(
                                                                normalizedQuery
                                                        )



                                        ||

                                        /*
                                         * TYPE
                                         */

                                        event.getType() != null

                                                &&

                                                event.getType()
                                                        .toLowerCase()
                                                        .contains(
                                                                normalizedQuery
                                                        )



                                        ||

                                        /*
                                         * TAGS
                                         */

                                        event.getTags() != null

                                                &&

                                                event.getTags()

                                                        .stream()

                                                        .anyMatch(tag ->

                                                                tag.toLowerCase()

                                                                        .contains(
                                                                                normalizedQuery
                                                                        )
                                                        )
                        )

                        .toList();



        return filteredEvents

                .stream()

                .map(this::mapToListResponse)

                .collect(Collectors.toList());
    }



    /*
     * =========================================================
     * FILTER BY TYPE
     * =========================================================
     */

    public List<EventListResponse> filterByType(
            String type
    ) {

        return eventRepository

                .findByType(type)

                .stream()

                .map(this::mapToListResponse)

                .collect(Collectors.toList());
    }



    /*
     * =========================================================
     * FILTER BY STATUS
     * =========================================================
     */

    public List<EventListResponse> filterByStatus(
            String status
    ) {

        return eventRepository

                .findByStatus(status)

                .stream()

                .map(this::mapToListResponse)

                .collect(Collectors.toList());
    }



    /*
     * =========================================================
     * GET EVENT BY ID
     * =========================================================
     */

    public Event getEventById(
            String id
    ) {

        return eventRepository

                .findById(id)

                .orElseThrow(

                        () -> new RuntimeException(
                                "Event not found"
                        )
                );
    }



    /*
     * =========================================================
     * DELETE EVENT
     * =========================================================
     *
     * Soft delete
     *
     */

    public EventResponse cancelEvent(
            String id
    ) {

        Event event =
                getEventById(id);



        event.setStatus(
                "CANCELLED"
        );



        Event updatedEvent =
                eventRepository.save(event);



        return new EventResponse(

                updatedEvent.getId(),

                updatedEvent.getTitle(),

                updatedEvent.getStatus()
        );
    }



    /*
     * =========================================================
     * UPDATE EVENT
     * =========================================================
     */

    public EventResponse updateEvent(

            String id,

            CreateEventRequest request
    ) {

        Event event =
                getEventById(id);



        event.setTitle(
                request.getTitle()
        );



        event.setDescription(
                request.getDescription()
        );



        event.setType(
                request.getType()
        );



        event.setLocation(
                request.getLocation()
        );



        event.setCapacity(
                request.getCapacity()
        );



        event.setStartDate(
                request.getStartDate()
        );



        event.setEndDate(
                request.getEndDate()
        );



        event.setTags(
                request.getTags()
        );



        event.setMetadata(
                request.getMetadata()
        );



        Event updatedEvent =
                eventRepository.save(event);



        return new EventResponse(

                updatedEvent.getId(),

                updatedEvent.getTitle(),

                updatedEvent.getStatus()
        );
    }



    /*
     * =========================================================
     * PRIVATE MAPPER
     * =========================================================
     */

    private EventListResponse mapToListResponse(
            Event event
    ) {

        return new EventListResponse(

                event.getId(),

                event.getTitle(),

                event.getType(),

                event.getLocation(),

                event.getCapacity(),

                event.getStartDate(),

                event.getStatus()
        );
    }
}