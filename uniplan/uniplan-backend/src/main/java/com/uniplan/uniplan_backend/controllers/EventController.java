package com.uniplan.uniplan_backend.controllers;

import com.uniplan.uniplan_backend.dto.CreateEventRequest;

import com.uniplan.uniplan_backend.dto.EventListResponse;

import com.uniplan.uniplan_backend.dto.EventResponse;

import com.uniplan.uniplan_backend.model.document.embedded.Event;

import com.uniplan.uniplan_backend.services.EventService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@CrossOrigin
public class EventController {

    private final EventService eventService;



    /*
     * =========================================================
     * CREATE EVENT
     * =========================================================
     */

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(

            @RequestBody
            CreateEventRequest request
    ) {

        return ResponseEntity.ok(

                eventService.createEvent(request)
        );
    }



    /*
     * =========================================================
     * GET ALL EVENTS
     * =========================================================
     */

    @GetMapping
    public ResponseEntity<List<EventListResponse>>
    getAllEvents() {

        return ResponseEntity.ok(

                eventService.getAllEvents()
        );
    }



    /*
     * =========================================================
     * SEARCH EVENTS
     * =========================================================
     *
     * Example:
     * /events/search?query=hackathon
     *
     */

    @GetMapping("/search")
    public ResponseEntity<List<EventListResponse>>
    searchEvents(

            @RequestParam
            String query
    ) {

        return ResponseEntity.ok(

                eventService.searchEvents(query)
        );
    }



    /*
     * =========================================================
     * FILTER BY TYPE
     * =========================================================
     *
     * Example:
     * /events/type/WORKSHOP
     *
     */

    @GetMapping("/type/{type}")
    public ResponseEntity<List<EventListResponse>>
    filterByType(

            @PathVariable
            String type
    ) {

        return ResponseEntity.ok(

                eventService.filterByType(type)
        );
    }



    /*
     * =========================================================
     * FILTER BY STATUS
     * =========================================================
     *
     * Example:
     * /events/status/ACTIVE
     *
     */

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventListResponse>>
    filterByStatus(

            @PathVariable
            String status
    ) {

        return ResponseEntity.ok(

                eventService.filterByStatus(status)
        );
    }



    /*
     * =========================================================
     * GET EVENT BY ID
     * =========================================================
     */

    @GetMapping("/{id}")
    public ResponseEntity<Event>
    getEventById(

            @PathVariable
            String id
    ) {

        return ResponseEntity.ok(

                eventService.getEventById(id)
        );
    }



    /*
     * =========================================================
     * UPDATE EVENT
     * =========================================================
     */

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse>
    updateEvent(

            @PathVariable
            String id,

            @RequestBody
            CreateEventRequest request
    ) {

        return ResponseEntity.ok(

                eventService.updateEvent(
                        id,
                        request
                )
        );
    }



    /*
     * =========================================================
     * CANCEL EVENT
     * =========================================================
     *
     * Soft delete
     *
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<EventResponse>
    cancelEvent(

            @PathVariable
            String id
    ) {

        return ResponseEntity.ok(

                eventService.cancelEvent(id)
        );
    }
}