package in.ignishers.eduportal.repos.institutional;

import in.ignishers.eduportal.models.institutional.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByCollegeIdAndCode(Long collegeId, String code);

    List<Department> findAllByCollegeIdAndDeletedFalse(Long collegeId);

    Optional<Department> findByIdAndDeletedFalse(Long id);
}
