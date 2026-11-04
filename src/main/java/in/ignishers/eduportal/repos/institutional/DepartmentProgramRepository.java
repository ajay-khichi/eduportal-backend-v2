package in.ignishers.eduportal.repos.institutional;

import in.ignishers.eduportal.models.institutional.DepartmentProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentProgramRepository extends JpaRepository<DepartmentProgram, Long> {

    boolean existsByDepartmentIdAndProgramId(Long deptId, Long progId);

    Optional<DepartmentProgram> findByDepartmentIdAndProgramId(Long deptId, Long progId);

    List<DepartmentProgram> findAllByDepartmentIdAndDeletedFalse(Long deptId);

    Optional<DepartmentProgram> findByIdAndDeletedFalse(Long id);
}
