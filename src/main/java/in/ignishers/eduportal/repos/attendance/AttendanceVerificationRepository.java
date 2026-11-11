package in.ignishers.eduportal.repos.attendance;

import in.ignishers.eduportal.models.attendance.AttendanceVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceVerificationRepository extends JpaRepository<AttendanceVerification, Long> {

    Optional<AttendanceVerification> findByAttendanceId(Long attendanceId);
}
