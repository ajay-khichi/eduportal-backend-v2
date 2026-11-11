package in.ignishers.eduportal.repos.attendance;

import in.ignishers.eduportal.models.attendance.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByClassSessionIdAndStudentId(Long classSessionId, Long studentId);

    Optional<Attendance> findByClassSessionIdAndStudentId(Long classSessionId, Long studentId);

    List<Attendance> findAllByClassSessionIdOrderByStudentEnrollmentNumberAsc(Long classSessionId);

    List<Attendance> findAllByStudentIdOrderByMarkedAtDesc(Long studentId);

    @Query("""
            SELECT a.classSession.teachingAssignment.subject.id AS subjectId,
                   a.classSession.teachingAssignment.subject.name AS subjectName,
                   a.classSession.teachingAssignment.subject.code AS subjectCode,
                   COUNT(a.id) AS totalClasses,
                   SUM(CASE WHEN a.status IN (
                       in.ignishers.eduportal.enums.AttendanceStatus.PRESENT,
                       in.ignishers.eduportal.enums.AttendanceStatus.LATE
                   ) THEN 1 ELSE 0 END) AS attendedClasses
            FROM Attendance a
            WHERE a.student.id = :studentId
              AND a.classSession.teachingAssignment.academicSession.id = :academicSessionId
            GROUP BY a.classSession.teachingAssignment.subject.id,
                     a.classSession.teachingAssignment.subject.name,
                     a.classSession.teachingAssignment.subject.code
            """)
    List<SubjectAttendanceStats> getStudentAttendanceStatsBySession(
            @Param("studentId") Long studentId,
            @Param("academicSessionId") Long academicSessionId
    );

    interface SubjectAttendanceStats {
        Long getSubjectId();
        String getSubjectName();
        String getSubjectCode();
        Long getTotalClasses();
        Long getAttendedClasses();
    }
}
