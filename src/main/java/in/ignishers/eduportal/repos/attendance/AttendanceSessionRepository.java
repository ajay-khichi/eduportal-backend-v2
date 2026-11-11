package in.ignishers.eduportal.repos.attendance;

import in.ignishers.eduportal.models.attendance.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    Optional<AttendanceSession> findByClassSessionId(Long classSessionId);

    boolean existsByClassSessionId(Long classSessionId);

    /**
     * Ultra-lightweight projection for the 5-second rolling QR endpoint.
     * Fetches only required scalar columns without hydrating heavy entity graphs.
     */
    @Query("""
            SELECT s.id AS id,
                   s.secret AS secret,
                   s.active AS active,
                   s.expiresAt AS expiresAt,
                   s.classSession.teachingAssignment.faculty.user.id AS facultyUserId
            FROM AttendanceSession s
            WHERE s.id = :sessionId
            """)
    Optional<QrSessionProjection> findQrProjectionById(@Param("sessionId") Long sessionId);

    interface QrSessionProjection {
        Long getId();
        String getSecret();
        boolean isActive();
        LocalDateTime getExpiresAt();
        Long getFacultyUserId();
    }
}
