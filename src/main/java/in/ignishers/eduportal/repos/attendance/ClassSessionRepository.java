package in.ignishers.eduportal.repos.attendance;

import in.ignishers.eduportal.models.attendance.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    List<ClassSession> findAllByTeachingAssignmentSectionIdAndClassDateBetweenOrderByClassDateAscStartTimeAsc(
            Long sectionId, LocalDate startDate, LocalDate endDate);

    List<ClassSession> findAllByTeachingAssignmentFacultyIdAndClassDateBetweenOrderByClassDateAscStartTimeAsc(
            Long facultyId, LocalDate startDate, LocalDate endDate);

    List<ClassSession> findAllByTeachingAssignmentIdOrderByClassDateAscStartTimeAsc(Long teachingAssignmentId);

    @Query("""
            SELECT COUNT(cs) > 0 FROM ClassSession cs
            WHERE cs.teachingAssignment.faculty.id = :facultyId
              AND cs.classDate = :classDate
              AND cs.status <> in.ignishers.eduportal.enums.ClassStatus.CANCELLED
              AND cs.startTime < :endTime
              AND cs.endTime > :startTime
              AND (:excludeId IS NULL OR cs.id <> :excludeId)
            """)
    boolean existsFacultyConflict(
            @Param("facultyId") Long facultyId,
            @Param("classDate") LocalDate classDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("""
            SELECT COUNT(cs) > 0 FROM ClassSession cs
            WHERE cs.teachingAssignment.section.id = :sectionId
              AND cs.classDate = :classDate
              AND cs.status <> in.ignishers.eduportal.enums.ClassStatus.CANCELLED
              AND cs.startTime < :endTime
              AND cs.endTime > :startTime
              AND (:excludeId IS NULL OR cs.id <> :excludeId)
            """)
    boolean existsSectionConflict(
            @Param("sectionId") Long sectionId,
            @Param("classDate") LocalDate classDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("""
            SELECT COUNT(cs) > 0 FROM ClassSession cs
            WHERE cs.teachingAssignment.faculty.college.id = :collegeId
              AND LOWER(cs.room) = LOWER(:room)
              AND cs.classDate = :classDate
              AND cs.status <> in.ignishers.eduportal.enums.ClassStatus.CANCELLED
              AND cs.startTime < :endTime
              AND cs.endTime > :startTime
              AND (:excludeId IS NULL OR cs.id <> :excludeId)
            """)
    boolean existsRoomConflict(
            @Param("collegeId") Long collegeId,
            @Param("room") String room,
            @Param("classDate") LocalDate classDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
