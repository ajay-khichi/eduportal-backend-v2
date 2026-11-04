package in.ignishers.eduportal.repos.institutional;

import in.ignishers.eduportal.models.institutional.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    boolean existsByCode(String code);

    Optional<Program> findByCode(String code);

    List<Program> findAllByDeletedFalse();

    Optional<Program> findByIdAndDeletedFalse(Long id);
}
