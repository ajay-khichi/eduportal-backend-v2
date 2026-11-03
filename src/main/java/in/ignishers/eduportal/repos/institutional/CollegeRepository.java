package in.ignishers.eduportal.repos.institutional;

import in.ignishers.eduportal.models.institutional.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    boolean existsByCode(String code);

    Optional<College> findByCode(String code);

    List<College> findAllByDeletedFalse();

    Optional<College> findByIdAndDeletedFalse(Long id);

    Optional<College> findByAdminId(Long adminUserId);
}
