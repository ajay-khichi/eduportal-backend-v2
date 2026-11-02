package in.ignishers.eduportal.repos.institutional;

import in.ignishers.eduportal.models.institutional.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {
}
