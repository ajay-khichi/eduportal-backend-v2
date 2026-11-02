package in.ignishers.eduportal.repos.attendance;

import in.ignishers.eduportal.models.attendance.FaceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaceProfileRepository extends JpaRepository<FaceProfile, Long> {
}
