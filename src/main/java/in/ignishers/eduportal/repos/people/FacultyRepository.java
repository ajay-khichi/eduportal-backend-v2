package in.ignishers.eduportal.repos.people;

import in.ignishers.eduportal.models.people.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
}
