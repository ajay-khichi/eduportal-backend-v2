package in.ignishers.eduportal.repos.institutional;

import in.ignishers.eduportal.models.institutional.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
