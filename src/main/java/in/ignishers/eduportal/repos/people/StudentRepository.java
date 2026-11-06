package in.ignishers.eduportal.repos.people;

import in.ignishers.eduportal.models.people.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findAllByCollegeIdAndDeletedFalse(Long collegeId);

    List<Student> findAllByDepartmentIdAndDeletedFalse(Long departmentId);

    Optional<Student> findByIdAndDeletedFalse(Long id);

    Optional<Student> findByUserIdAndDeletedFalse(Long userId);

    Optional<Student> findByUserEmailIgnoreCaseAndDeletedFalse(String email);

    boolean existsByCollegeIdAndEnrollmentNumberIgnoreCaseAndDeletedFalse(Long collegeId, String enrollmentNumber);
}