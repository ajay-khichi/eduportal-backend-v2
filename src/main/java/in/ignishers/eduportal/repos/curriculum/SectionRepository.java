package in.ignishers.eduportal.repos.curriculum;

import in.ignishers.eduportal.models.curriculum.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
}
