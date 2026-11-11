package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.people.BulkEnrollmentRequest;
import in.ignishers.eduportal.dto.people.EnrollmentRequest;
import in.ignishers.eduportal.dto.people.EnrollmentResponse;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.curriculum.Section;
import in.ignishers.eduportal.models.people.Enrollment;
import in.ignishers.eduportal.models.people.Student;
import in.ignishers.eduportal.repos.curriculum.SectionRepository;
import in.ignishers.eduportal.repos.people.EnrollmentRepository;
import in.ignishers.eduportal.repos.people.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepo;
    private final StudentRepository studentRepo;
    private final SectionRepository sectionRepo;

    @Transactional
    public EnrollmentResponse enrollStudent(Long sectionId, EnrollmentRequest request) {
        Section section = sectionRepo.findByIdAndDeletedFalse(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + sectionId));

        Student student = studentRepo.findByIdAndDeletedFalse(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.studentId()));

        validateEnrollment(student, section);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment = enrollmentRepo.save(enrollment);
        return toResponse(enrollment);
    }

    @Transactional
    public List<EnrollmentResponse> enrollBulk(Long sectionId, BulkEnrollmentRequest request) {
        Section section = sectionRepo.findByIdAndDeletedFalse(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + sectionId));

        List<EnrollmentResponse> responses = new ArrayList<>();

        for(Long studentId : request.studentIds()) {
            Student student = studentRepo.findByIdAndDeletedFalse(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

            validateEnrollment(student, section);

            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setSection(section);
            enrollment = enrollmentRepo.save(enrollment);

            responses.add(toResponse(enrollment));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsBySection(Long sectionId) {
        if (!sectionRepo.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section not found with id: " + sectionId);
        }
        return enrollmentRepo.findAllBySectionId(sectionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        if (!studentRepo.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        return enrollmentRepo.findAllByStudentId(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void unenrollStudent(Long sectionId, Long studentId) {
        Enrollment enrollment = enrollmentRepo.findByStudentIdAndSectionId(studentId, sectionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found for student id " + studentId + " in section id " + sectionId));
        enrollmentRepo.delete(enrollment);
    }

    private void validateEnrollment(Student student, Section section) {

        Long studentCollegeId = student.getCollege().getId();
        Long sectionCollegeId = section.getDepartmentProgram().getDepartment().getCollege().getId();
        // Cross-college validation
        if(!studentCollegeId.equals(sectionCollegeId)) {
            throw new IllegalArgumentException("Student does not belong to the same college as the section");
        }

        // Duplicate enrollment check in same section
        if (enrollmentRepo.existsByStudentIdAndSectionId(student.getId(), section.getId())) {
            throw new IllegalArgumentException(
                    "Student '" + student.getEnrollmentNumber() + "' is already enrolled in section '" + section.getName() + "'");
        }

        // Student already enrolled in any section of the same academic session
        if (enrollmentRepo.isStudentEnrolledInSession(student.getId(), section.getAcademicSession().getId())) {
            throw new IllegalArgumentException(
                    "Student '" + student.getEnrollmentNumber() + "' is already enrolled in another section for session '"
                            + section.getAcademicSession().getName() + "'");
        }
    }

    private EnrollmentResponse toResponse(Enrollment e) {
        Student s = e.getStudent();
        Section sec = e.getSection();
        return new EnrollmentResponse(
                e.getId(),
                s.getId(),
                s.getFirstName() + (s.getLastName() != null ? " " + s.getLastName() : ""),
                s.getEnrollmentNumber(),
                s.getUser().getEmail(),
                sec.getId(),
                sec.getName(),
                sec.getSemester(),
                sec.getDepartmentProgram().getProgram().getName(),
                sec.getAcademicSession().getId(),
                sec.getAcademicSession().getName(),
                e.getCreatedAt()
        );
    }
}
