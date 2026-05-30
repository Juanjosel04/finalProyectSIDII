package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.document.AttendanceRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository
        extends MongoRepository<AttendanceRecord, String> {

    List<AttendanceRecord> findByEventId(String eventId);

    Optional<AttendanceRecord> findByRegistrationId(String registrationId);

    List<AttendanceRecord> findByStudentUserId(String userId);

    List<AttendanceRecord> findByEventIdAndAttendanceStatus(String eventId, String status);

    long countByEventIdAndAttendanceStatus(String eventId, String status);
}
