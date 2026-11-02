package in.ignishers.eduportal.repos.institutional;

import in.ignishers.eduportal.models.institutional.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
}
