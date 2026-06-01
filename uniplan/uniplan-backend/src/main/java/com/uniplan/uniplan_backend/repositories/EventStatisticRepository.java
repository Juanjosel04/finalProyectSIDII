package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.relational.uniplan.EventStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStatisticRepository extends JpaRepository<EventStatistic, String> {

    List<EventStatistic> findByEventType(String eventType);

    List<EventStatistic> findByEventStatus(String eventStatus);

    List<EventStatistic> findAllByOrderByRegisteredDesc();

    @Query("SELECT e FROM EventStatistic e WHERE e.registered = 0 AND e.eventStatus = 'ACTIVE'")
    List<EventStatistic> findActiveEventsWithNoRegistrations();

    @Query("SELECT e FROM EventStatistic e WHERE e.attendanceRate < 30 AND (e.registered + e.attended) > 0")
    List<EventStatistic> findLowAttendanceEvents();
}
