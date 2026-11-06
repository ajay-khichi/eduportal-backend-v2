package in.ignishers.eduportal.repos.curriculum;

import in.ignishers.eduportal.models.curriculum.AcademicSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicSessionRepository extends JpaRepository<AcademicSession, Long> {

    List<AcademicSession> findAllByCollegeIdAndDeletedFalse(Long collegeId);

    Optional<AcademicSession> findByIdAndDeletedFalse(Long id);

    Optional<AcademicSession> findByCollegeIdAndCurrentTrueAndDeletedFalse(Long collegeId);

    boolean existsByCollegeIdAndNameIgnoreCaseAndDeletedFalse(Long collegeId, String name);

    // Atomically reset all sessions for this college to current = false
    @Modifying(clearAutomatically = true)
    @Query("UPDATE AcademicSession s SET s.current = false WHERE s.college.id = :collegeId AND s.deleted = false")
    void resetCurrentSessionsForCollege(@Param("collegeId") Long collegeId);
}