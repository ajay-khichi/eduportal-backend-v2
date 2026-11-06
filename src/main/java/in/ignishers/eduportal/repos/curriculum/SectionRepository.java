package in.ignishers.eduportal.repos.curriculum;

import in.ignishers.eduportal.models.curriculum.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findAllByAcademicSessionIdAndDeletedFalse(Long academicSessionId);

    List<Section> findAllByDepartmentProgramIdAndAcademicSessionIdAndSemesterAndDeletedFalse(
            Long departmentProgramId, Long academicSessionId, Integer semester);

    Optional<Section> findByIdAndDeletedFalse(Long id);

    boolean existsByDepartmentProgramIdAndAcademicSessionIdAndSemesterAndNameIgnoreCaseAndDeletedFalse(
            Long departmentProgramId, Long academicSessionId, Integer semester, String name);
}