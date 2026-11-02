package in.ignishers.eduportal.repos.attendance;

import in.ignishers.eduportal.models.attendance.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
}
