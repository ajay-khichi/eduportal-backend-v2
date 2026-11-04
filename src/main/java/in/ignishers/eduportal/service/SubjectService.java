package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.curriculum.SubjectRequest;
import in.ignishers.eduportal.dto.curriculum.SubjectResponse;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.curriculum.Subject;
import in.ignishers.eduportal.models.institutional.DepartmentProgram;
import in.ignishers.eduportal.repos.curriculum.SubjectRepository;
import in.ignishers.eduportal.repos.institutional.DepartmentProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subRepo;
    private final DepartmentProgramRepository dpRepo;

    @Transactional
    public SubjectResponse createSubject(Long dpId, SubjectRequest request) {
        DepartmentProgram dp = dpRepo.findByIdAndDeletedFalse(dpId)
                .orElseThrow(() -> new ResourceNotFoundException("DepartmentProgram mapping not found with id: " + dpId));

        String code = request.code().trim().toUpperCase();

        if (subRepo.existsByDepartmentProgramIdAndCode(dpId, code)) {
            throw new IllegalArgumentException("Subject with code '" + code + "'  already exists in this program");
        }

        Subject subject = Subject.builder()
                .name(request.name().trim())
                .code(code)
                .semester(request.semester())
                .credits(request.credits())
                .departmentProgram(dp)
                .build();

        subject = subRepo.save(subject);
        return toResponse(subject);
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id) {
        Subject sub = subRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("Subject not found with id: "+ id));

        return toResponse(sub);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjectsByDepartmentProgram(Long dpId, Integer semester) {
        List<Subject> subjects;

        if (semester != null) {
            subjects = subRepo.findAllByDepartmentProgramIdAndSemesterAndDeletedFalse(dpId, semester);
        }
        else {
            subjects = subRepo.findAllByDepartmentProgramIdAndDeletedFalse(dpId);
        }
        return subjects
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        Subject sub = subRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("Subject not found with id: "+ id));

        String newCode = request.code().trim().toUpperCase();
        Long dpId = sub.getDepartmentProgram().getId();

        if(!sub.getCode().equalsIgnoreCase(newCode) && subRepo.existsByDepartmentProgramIdAndCode(dpId, newCode)) {
            throw new IllegalArgumentException("Subject with code '" + newCode + "' already exists in program");
        }

        sub.setName(request.name().trim());
        sub.setCode(newCode);
        sub.setCredits(request.credits());
        sub.setSemester(request.semester());

        sub = subRepo.save(sub);
        return toResponse(sub);
    }

    @Transactional
    public void deleteSubject(Long id) {
        Subject sub = subRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("Subject not found with id: "+ id));

        sub.setDeleted(true);
        subRepo.save(sub);
    }

    private SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getDepartmentProgram().getId(),
                subject.getDepartmentProgram().getProgram().getCode(),
                subject.getDepartmentProgram().getDepartment().getCode(),
                subject.getName(),
                subject.getCode(),
                subject.getSemester(),
                subject.getCredits(),
                subject.getCreatedAt()
        );
    }
}
