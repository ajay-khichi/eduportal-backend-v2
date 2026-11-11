package in.ignishers.eduportal.repos.people;

import in.ignishers.eduportal.models.people.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {

    List<TeachingAssignment> findAllByFacultyId(Long facultyId);

    List<TeachingAssignment> findAllByFacultyIdAndAcademicSessionId(Long facultyId, Long sessionId);

    List<TeachingAssignment> findAllBySectionId(Long sectionId);

    List<TeachingAssignment> findAllByAcademicSessionId(Long sessionId);

    boolean existsByFacultyIdAndSubjectIdAndSectionIdAndAcademicSessionId(
            Long facultyId, Long subjectId, Long sectionId, Long academicSessionId);

    boolean existsBySubjectIdAndSectionIdAndAcademicSessionId(
            Long subjectId, Long sectionId, Long academicSessionId);
}