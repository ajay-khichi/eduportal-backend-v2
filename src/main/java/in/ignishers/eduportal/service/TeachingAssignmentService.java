package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.people.TeachingAssignmentRequest;
import in.ignishers.eduportal.dto.people.TeachingAssignmentResponse;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.curriculum.AcademicSession;
import in.ignishers.eduportal.models.curriculum.Section;
import in.ignishers.eduportal.models.curriculum.Subject;
import in.ignishers.eduportal.models.people.Faculty;
import in.ignishers.eduportal.models.people.TeachingAssignment;
import in.ignishers.eduportal.repos.curriculum.AcademicSessionRepository;
import in.ignishers.eduportal.repos.curriculum.SectionRepository;
import in.ignishers.eduportal.repos.curriculum.SubjectRepository;
import in.ignishers.eduportal.repos.people.FacultyRepository;
import in.ignishers.eduportal.repos.people.TeachingAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeachingAssignmentService {

    private final TeachingAssignmentRepository assignmentRepo;
    private final FacultyRepository facultyRepo;
    private final SubjectRepository subjectRepo;
    private final SectionRepository sectionRepo;
    private final AcademicSessionRepository sessionRepo;

    @Transactional
    public TeachingAssignmentResponse createAssignment(TeachingAssignmentRequest request) {
        Faculty faculty = facultyRepo.findByIdAndDeletedFalse(request.facultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + request.facultyId()));

        Subject subject = subjectRepo.findByIdAndDeletedFalse(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.subjectId()));

        Section section = sectionRepo.findByIdAndDeletedFalse(request.sectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + request.sectionId()));

        AcademicSession session = sessionRepo.findByIdAndDeletedFalse(request.academicSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with id: " + request.academicSessionId()));

        // Validations
        if (!section.getAcademicSession().getId().equals(session.getId())) {
            throw new IllegalArgumentException("Section does not belong to the specified academic session");
        }

        Long collegeId = faculty.getCollege().getId();
        Long sectionCollegeId = section.getDepartmentProgram().getDepartment().getCollege().getId();
        if (!collegeId.equals(sectionCollegeId)) {
            throw new IllegalArgumentException("Faculty and Section belong to different colleges");
        }

        if (assignmentRepo.existsByFacultyIdAndSubjectIdAndSectionIdAndAcademicSessionId(
                faculty.getId(), subject.getId(), section.getId(), session.getId())) {
            throw new IllegalArgumentException("This teaching assignment already exists");
        }

        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setFaculty(faculty);
        assignment.setSubject(subject);
        assignment.setSection(section);
        assignment.setAcademicSession(session);

        assignment = assignmentRepo.save(assignment);
        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getAssignmentsByFaculty(Long facultyId) {
        if (!facultyRepo.existsById(facultyId)) {
            throw new ResourceNotFoundException("Faculty not found with id: " + facultyId);
        }
        return assignmentRepo.findAllByFacultyId(facultyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getAssignmentsByFacultyAndSession(Long facultyId, Long sessionId) {
        return assignmentRepo.findAllByFacultyIdAndAcademicSessionId(facultyId, sessionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getAssignmentsBySection(Long sectionId) {
        if (!sectionRepo.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section not found with id: " + sectionId);
        }
        return assignmentRepo.findAllBySectionId(sectionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    @Transactional
    public void deleteAssignment(Long id) {
        TeachingAssignment assignment = assignmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found with id: " + id));
        assignmentRepo.delete(assignment);
    }
    private TeachingAssignmentResponse toResponse(TeachingAssignment a) {
        Faculty f = a.getFaculty();
        Subject sub = a.getSubject();
        Section sec = a.getSection();
        AcademicSession ses = a.getAcademicSession();
        return new TeachingAssignmentResponse(
                a.getId(),
                f.getId(),
                f.getFirstName() + (f.getLastName() != null ? " " + f.getLastName() : ""),
                f.getEmployeeNumber(),
                sub.getId(),
                sub.getName(),
                sub.getCode(),
                sec.getId(),
                sec.getName(),
                sec.getSemester(),
                ses.getId(),
                ses.getName(),
                a.getCreatedAt()
        );
    }
}
