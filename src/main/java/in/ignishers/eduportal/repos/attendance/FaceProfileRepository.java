package in.ignishers.eduportal.repos.attendance;

import in.ignishers.eduportal.models.attendance.FaceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FaceProfileRepository extends JpaRepository<FaceProfile, Long> {

    Optional<FaceProfile> findByStudentId(Long studentId);

    Optional<FaceProfile> findByStudentIdAndActiveTrue(Long studentId);

    boolean existsByStudentIdAndActiveTrue(Long studentId);
}
