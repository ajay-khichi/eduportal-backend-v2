package in.ignishers.eduportal.repos.curriculum;

import in.ignishers.eduportal.models.curriculum.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
