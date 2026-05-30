package com.uniplan.uniplan_backend.model.document;

import com.uniplan.uniplan_backend.model.document.embedded.StudentSnapshot;
import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MongoDB document: attendance_records
 *
 * Tracks check-in / check-out for each registered student per event.
 */
@Document(collection = "attendance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    private String id;

    /*
     * MongoDB _id of the event
     */
    @Indexed
    private String eventId;

    private String eventCode;

    /*
     * MongoDB _id of the EventRegistrationDocument
     */
    @Indexed
    private String registrationId;

    /*
     * Student snapshot (same as in registration)
     */
    private StudentSnapshot student;

    /*
     * PRESENT | ABSENT | LATE | EXCUSED
     */
    private String attendanceStatus;

    /*
     * Check-in / check-out timestamps
     */
    private LocalDateTime checkedInAt;

    private LocalDateTime checkedOutAt;

    /*
     * Who registered the attendance
     * { userId, name, role }
     */
    private Map<String, Object> checkedBy;

    /*
     * QR | MANUAL | NFC | BIOMETRIC
     */
    private String method;

    /*
     * Supporting evidence
     * Example: { qrCode: "...", photoUrl: "..." }
     */
    private Map<String, Object> evidence;

    private String notes;

    /*
     * Audit
     */
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
