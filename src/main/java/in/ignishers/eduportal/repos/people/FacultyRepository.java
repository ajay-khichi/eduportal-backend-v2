package in.ignishers.eduportal.repos.people;

import in.ignishers.eduportal.models.people.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByUserEmail(String email);

    Optional<Faculty> findByCollegeIdAndEmployeeNumber(Long collegeId, String employeeNumber);
}
