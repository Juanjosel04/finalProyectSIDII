package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.document.embedded.*;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface EventRepository
        extends MongoRepository<Event, String> {

    List<Event> findByStatus(
            String status
    );



    List<Event> findByType(
            String type
    );

    List<Event> findByTitleContainingIgnoreCase(
            String title
    );

    List<Event> findByLocationContainingIgnoreCase(
            String location
    );
}