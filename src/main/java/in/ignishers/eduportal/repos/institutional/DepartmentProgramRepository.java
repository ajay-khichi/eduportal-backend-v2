package in.ignishers.eduportal.repos.institutional;

import in.ignishers.eduportal.models.institutional.DepartmentProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentProgramRepository extends JpaRepository<DepartmentProgram, Long> {
}
