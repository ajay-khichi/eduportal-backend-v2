package in.ignishers.eduportal.repos.curriculum;

import in.ignishers.eduportal.models.curriculum.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByDepartmentProgramIdAndCode(Long deptProgId, String code);

    Optional<Subject> findByDepartmentProgramIdAndCodeAndDeletedFalse(Long deptProgId, String code);

    List<Subject> findAllByDepartmentProgramIdAndDeletedFalse(Long deptProgId);

    List<Subject> findAllByDepartmentProgramIdAndSemesterAndDeletedFalse(Long departmentProgramId, Integer semester);

    Optional<Subject> findByIdAndDeletedFalse(Long id);
}
