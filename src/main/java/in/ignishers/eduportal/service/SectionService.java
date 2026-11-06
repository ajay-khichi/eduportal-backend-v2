package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.curriculum.SectionRequest;
import in.ignishers.eduportal.dto.curriculum.SectionResponse;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.curriculum.AcademicSession;
import in.ignishers.eduportal.models.curriculum.Section;
import in.ignishers.eduportal.models.institutional.DepartmentProgram;
import in.ignishers.eduportal.repos.curriculum.AcademicSessionRepository;
import in.ignishers.eduportal.repos.curriculum.SectionRepository;
import in.ignishers.eduportal.repos.institutional.DepartmentProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepo;
    private final DepartmentProgramRepository deptProgramRepo;
    private final AcademicSessionRepository sessionRepo;

    @Transactional
    public SectionResponse createSection(SectionRequest request) {
        DepartmentProgram deptProgram = deptProgramRepo.findByIdAndDeletedFalse(request.departmentProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("DepartmentProgram not found with id: " + request.departmentProgramId()));

        AcademicSession session = sessionRepo.findByIdAndDeletedFalse(request.academicSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with id: " + request.academicSessionId()));

        String sectionName = request.name().trim().toUpperCase();

        if (sectionRepo.existsByDepartmentProgramIdAndAcademicSessionIdAndSemesterAndNameIgnoreCaseAndDeletedFalse(
                deptProgram.getId(), session.getId(), request.semester(), sectionName)) {
            throw new IllegalArgumentException("Section '" + sectionName + "' already exists for this program, session, and semester");
        }

        Section section = new Section();
        section.setDepartmentProgram(deptProgram);
        section.setAcademicSession(session);
        section.setSemester(request.semester());
        section.setName(sectionName);

        section = sectionRepo.save(section);
        return toResponse(section);
    }

    @Transactional(readOnly = true)
    public SectionResponse getSectionById(Long id) {
        Section section = sectionRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
        return toResponse(section);
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> getSectionsBySession(Long sessionId) {
        if (!sessionRepo.existsById(sessionId)) {
            throw new ResourceNotFoundException("Academic session not found with id: " + sessionId);
        }
        return sectionRepo.findAllByAcademicSessionIdAndDeletedFalse(sessionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> getSections(Long departmentProgramId, Long sessionId, Integer semester) {
        return sectionRepo.findAllByDepartmentProgramIdAndAcademicSessionIdAndSemesterAndDeletedFalse(
                        departmentProgramId, sessionId, semester)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SectionResponse updateSection(Long id, SectionRequest request) {
        Section section = sectionRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));

        DepartmentProgram deptProgram = deptProgramRepo.findByIdAndDeletedFalse(request.departmentProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("DepartmentProgram not found with id: " + request.departmentProgramId()));

        AcademicSession session = sessionRepo.findByIdAndDeletedFalse(request.academicSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with id: " + request.academicSessionId()));

        String newName = request.name().trim().toUpperCase();

        boolean identityChanged = !section.getDepartmentProgram().getId().equals(deptProgram.getId())
                || !section.getAcademicSession().getId().equals(session.getId())
                || !section.getSemester().equals(request.semester())
                || !section.getName().equalsIgnoreCase(newName);

        if (identityChanged && sectionRepo.existsByDepartmentProgramIdAndAcademicSessionIdAndSemesterAndNameIgnoreCaseAndDeletedFalse(
                deptProgram.getId(), session.getId(), request.semester(), newName)) {
            throw new IllegalArgumentException("Section '" + newName + "' already exists for this program, session, and semester");
        }

        section.setDepartmentProgram(deptProgram);
        section.setAcademicSession(session);
        section.setSemester(request.semester());
        section.setName(newName);

        return toResponse(section);
    }

    @Transactional
    public void deleteSection(Long id) {
        Section section = sectionRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
        section.setDeleted(true);
    }

    private SectionResponse toResponse(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getDepartmentProgram().getId(),
                section.getDepartmentProgram().getDepartment().getName(),
                section.getDepartmentProgram().getProgram().getName(),
                section.getDepartmentProgram().getProgram().getCode(),
                section.getAcademicSession().getId(),
                section.getAcademicSession().getName(),
                section.getSemester(),
                section.getName(),
                section.getCreatedAt(),
                section.getUpdatedAt()
        );
    }
}