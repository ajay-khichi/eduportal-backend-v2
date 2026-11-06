package in.ignishers.eduportal.repos.people;

import in.ignishers.eduportal.models.people.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    List<Faculty> findAllByCollegeIdAndDeletedFalse(Long collegeId);

    List<Faculty> findAllByDepartmentIdAndDeletedFalse(Long departmentId);

    Optional<Faculty> findByIdAndDeletedFalse(Long id);

    Optional<Faculty> findByUserIdAndDeletedFalse(Long userId);

    Optional<Faculty> findByUserEmailIgnoreCaseAndDeletedFalse(String email);

    boolean existsByCollegeIdAndEmployeeNumberIgnoreCaseAndDeletedFalse(Long collegeId, String employeeNumber);
}