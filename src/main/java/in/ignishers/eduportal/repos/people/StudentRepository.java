package in.ignishers.eduportal.repos.people;

import in.ignishers.eduportal.models.people.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
}
