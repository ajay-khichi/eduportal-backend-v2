package in.ignishers.eduportal.repos.people;

import in.ignishers.eduportal.models.people.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findAllBySectionId(Long sectionId);

    List<Enrollment> findAllByStudentId(Long studentId);

    Optional<Enrollment> findByStudentIdAndSectionId(Long studentId, Long sectionId);

    boolean existsByStudentIdAndSectionId(Long studentId, Long sectionId);

    // Check if student is already enrolled in any section of the same academic session
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e " +
            "WHERE e.student.id = :studentId " +
            "AND e.section.academicSession.id = :sessionId")
    boolean isStudentEnrolledInSession(@Param("studentId") Long studentId, @Param("sessionId") Long sessionId);
}