package com.uniplan.uniplan_backend.repositories;

import com.uniplan.uniplan_backend.model.relational.university.Enrollment;
import com.uniplan.uniplan_backend.model.relational.university.EnrollmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    @Query("SELECT COUNT(e) > 0 FROM Enrollment e " +
           "WHERE e.student.id = :studentId AND e.group.subject.code = :subjectCode")
    boolean existsByStudentIdAndSubjectCode(
            @Param("studentId") String studentId,
            @Param("subjectCode") String subjectCode);

    @Query("SELECT COUNT(DISTINCT e.group.semester) FROM Enrollment e WHERE e.student.id = :studentId")
    long countDistinctSemestersByStudentId(@Param("studentId") String studentId);
}
